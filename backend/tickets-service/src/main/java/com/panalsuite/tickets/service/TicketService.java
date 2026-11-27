package com.panalsuite.tickets.service;

import com.panalsuite.tickets.dto.*;
import com.panalsuite.tickets.exception.ApiException;
import com.panalsuite.tickets.model.*;
import com.panalsuite.tickets.repository.LocationRepository;
import com.panalsuite.tickets.repository.TicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final LocationRepository locations;
    private final SequenceService sequences;
    private final MongoTemplate mongo;

    public TicketService(TicketRepository tickets, LocationRepository locations,
                         SequenceService sequences, MongoTemplate mongo) {
        this.tickets = tickets;
        this.locations = locations;
        this.sequences = sequences;
        this.mongo = mongo;
    }

    // ---------- consultas ----------

    public Ticket get(Long id) {
        return tickets.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket no encontrado: " + id));
    }

    // Todos los filtros son opcionales y se combinan con AND (mas nuevo primero)
    public List<Ticket> search(Long userId, Long assignedTo, List<TicketStatus> statuses,
                               List<Urgency> priorities, String q, LocalDate from, LocalDate to) {
        List<Criteria> filters = new ArrayList<>();
        if (userId != null) filters.add(Criteria.where("idUser").is(userId));
        if (assignedTo != null) filters.add(Criteria.where("peopleInCharge").is(assignedTo));
        if (statuses != null && !statuses.isEmpty()) filters.add(Criteria.where("status").in(statuses));
        if (priorities != null && !priorities.isEmpty()) filters.add(Criteria.where("priority").in(priorities));
        if (q != null && !q.isBlank()) {
            String regex = Pattern.quote(q.trim());
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(regex, "i"),
                    Criteria.where("description").regex(regex, "i")));
        }
        // Si las fechas vienen al reves se invierten (igual que la app)
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        if (from != null) filters.add(Criteria.where("createdAt").gte(from.atStartOfDay()));
        if (to != null) filters.add(Criteria.where("createdAt").lt(to.plusDays(1).atStartOfDay()));

        Query query = new Query();
        if (!filters.isEmpty()) query.addCriteria(new Criteria().andOperator(filters));
        query.with(Sort.by(Sort.Direction.DESC, "idTicket"));
        return mongo.find(query, Ticket.class);
    }

    public Map<String, Object> stats() {
        List<Ticket> all = tickets.findAll();
        List<Ticket> open = all.stream().filter(t -> t.getStatus() != TicketStatus.Closed).toList();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TicketStatus s : TicketStatus.values()) {
            byStatus.put(s.name(), all.stream().filter(t -> t.getStatus() == s).count());
        }
        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Urgency u : Urgency.values()) {
            byPriority.put(u.name(), all.stream().filter(t -> t.getPriority() == u).count());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", all.size());
        result.put("open", open.size());
        result.put("closed", all.size() - open.size());
        result.put("assigned", all.stream().filter(t -> t.getPeopleInCharge() != null).count());
        result.put("openUnassigned", open.stream().filter(t -> t.getPeopleInCharge() == null).count());
        result.put("openCritical", open.stream().filter(t -> t.getPriority() == Urgency.Critical).count());
        result.put("byStatus", byStatus);
        result.put("byPriority", byPriority);
        return result;
    }

    // ---------- escritura ----------

    public Ticket create(TicketCreateRequest req) {
        Location location = findLocation(req.branchId());
        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket();
        ticket.setIdTicket(sequences.next("tickets"));
        ticket.setIdUser(req.idUser());
        ticket.setTitle(req.title().trim());
        ticket.setDescription(req.description().trim());
        ticket.setCategory(req.category().trim());
        ticket.setType("");
        ticket.setImpact("");
        ticket.setPriority(req.priority());
        ticket.setStatus(TicketStatus.New);
        ticket.setCreatedAt(now);
        ticket.setLastUpdate(now);
        ticket.setBranch(new Branch(location.getIdLocation(), location.getName()));
        return tickets.save(ticket);
    }

    public Ticket update(Long id, TicketUpdateRequest req) {
        Ticket ticket = get(id);
        Location location = findLocation(req.branchId());
        LocalDateTime now = LocalDateTime.now();
        recordChange(ticket, "Title", ticket.getTitle(), req.title().trim(), req.actorId(), now);
        recordChange(ticket, "Priority", ticket.getPriority().name(), req.priority().name(), req.actorId(), now);
        ticket.setTitle(req.title().trim());
        ticket.setDescription(req.description().trim());
        ticket.setCategory(req.category().trim());
        ticket.setPriority(req.priority());
        ticket.setBranch(new Branch(location.getIdLocation(), location.getName()));
        ticket.setLastUpdate(now);
        return tickets.save(ticket);
    }

    public void delete(Long id) {
        tickets.delete(get(id));
    }

    public Ticket changeStatus(Long id, StatusRequest req) {
        Ticket ticket = get(id);
        if (ticket.getStatus() == req.status()) {
            throw new ApiException(HttpStatus.CONFLICT, "El ticket ya tiene ese estado");
        }
        LocalDateTime now = LocalDateTime.now();
        recordChange(ticket, "Status", ticket.getStatus().name(), req.status().name(), req.actorId(), now);
        ticket.setStatus(req.status());
        ticket.setLastUpdate(now);
        return tickets.save(ticket);
    }

    public Ticket assign(Long id, AssignRequest req) {
        Ticket ticket = get(id);
        if (req.operatorId().equals(ticket.getPeopleInCharge())) {
            throw new ApiException(HttpStatus.CONFLICT, "El ticket ya está asignado a ese operador");
        }
        LocalDateTime now = LocalDateTime.now();
        String previous = ticket.getPeopleInCharge() == null ? "Unassigned"
                : (req.previousOperatorName() == null || req.previousOperatorName().isBlank()
                ? "Operator #" + ticket.getPeopleInCharge() : req.previousOperatorName().trim());
        String next = req.operatorName() == null || req.operatorName().isBlank()
                ? "Operator #" + req.operatorId() : req.operatorName().trim();
        recordChange(ticket, "Assigned to", previous, next, req.actorId(), now);
        ticket.setPeopleInCharge(req.operatorId());
        ticket.setLastUpdate(now);
        return tickets.save(ticket);
    }

    // Deja sin asignar los tickets de un operador y registra el cambio en el historial
    public List<Ticket> releaseTicketsOf(ReleaseRequest req) {
        List<Ticket> affected = tickets.findByPeopleInCharge(req.operatorId());
        LocalDateTime now = LocalDateTime.now();
        String previous = req.operatorName() == null || req.operatorName().isBlank()
                ? "Operator #" + req.operatorId() : req.operatorName().trim();
        for (Ticket ticket : affected) {
            recordChange(ticket, "Assigned to", previous, "Unassigned", req.actorId(), now);
            ticket.setPeopleInCharge(null);
            ticket.setLastUpdate(now);
        }
        return tickets.saveAll(affected);
    }

    public Ticket setPinned(Long id, boolean pinned) {
        Ticket ticket = get(id);
        ticket.setPinned(pinned);
        return tickets.save(ticket);
    }

    public Ticket addComment(Long id, CommentRequest req) {
        Ticket ticket = get(id);
        LocalDateTime now = LocalDateTime.now();
        ticket.getCommentary().add(new Commentary(sequences.next("commentary"), req.authorId(), req.text().trim(), now));
        ticket.setLastUpdate(now);
        return tickets.save(ticket);
    }

    // ---------- helpers ----------

    private Location findLocation(Long id) {
        return locations.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Sucursal inexistente: " + id));
    }

    // Agrega una linea al historial solo si el valor realmente cambio
    private void recordChange(Ticket ticket, String field, String previous, String next, Long actorId, LocalDateTime when) {
        if (Objects.equals(previous, next)) return;
        ticket.getDevelopmentHistory().add(
                new DevelopmentEntry(sequences.next("development"), field, previous, next, actorId, when));
    }
}
