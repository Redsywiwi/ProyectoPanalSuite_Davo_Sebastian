package com.panalsuite.tickets.config;

import com.panalsuite.tickets.model.*;
import com.panalsuite.tickets.repository.CategoryRepository;
import com.panalsuite.tickets.repository.LocationRepository;
import com.panalsuite.tickets.repository.TicketRepository;
import com.panalsuite.tickets.service.SequenceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Parametros iniciales (mismos que la app) y 3 tickets de ejemplo si las colecciones estan vacias
@Component
public class DataSeeder implements CommandLineRunner {
    private final CategoryRepository categories;
    private final LocationRepository locations;
    private final TicketRepository tickets;
    private final SequenceService sequences;

    public DataSeeder(CategoryRepository categories, LocationRepository locations,
                      TicketRepository tickets, SequenceService sequences) {
        this.categories = categories;
        this.locations = locations;
        this.tickets = tickets;
        this.sequences = sequences;
    }

    @Override
    public void run(String... args) {
        if (categories.count() == 0) {
            List<String> names = List.of("Hardware", "Software", "Network", "Access", "Security", "Other");
            for (int i = 0; i < names.size(); i++) {
                categories.save(new Category((long) (i + 1), names.get(i)));
            }
            sequences.ensureAtLeast("categories", names.size());
        }
        if (locations.count() == 0) {
            List<String> names = List.of("Estación Central", "Plaza Egaña", "Tobalaba", "Manquehue", "Las Torres",
                    "Principe de Gales", "Los Dominicos", "Pedro de Valdivia", "Ñuñoa", "Los Heroes");
            for (int i = 0; i < names.size(); i++) {
                locations.save(new Location((long) (i + 1), names.get(i)));
            }
            sequences.ensureAtLeast("locations", names.size());
        }
        if (tickets.count() == 0) {
            seedTicket(1L, 1L, "Printer not working", "Other", Urgency.Low, TicketStatus.New, 1L, null);
            seedTicket(2L, 2L, "Hallway camera disconnected", "Hardware", Urgency.High, TicketStatus.InProgress, 3L, 4L);
            seedTicket(3L, 3L, "Cannot access VPN", "Network", Urgency.Critical, TicketStatus.Queued, 5L, 5L);
            sequences.ensureAtLeast("tickets", 3);
        }
    }

    private void seedTicket(Long id, Long idUser, String title, String category, Urgency priority,
                            TicketStatus status, Long locationId, Long operatorId) {
        Location location = locations.findById(locationId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        Ticket t = new Ticket();
        t.setIdTicket(id);
        t.setIdUser(idUser);
        t.setTitle(title);
        t.setDescription("Ticket de ejemplo: " + title);
        t.setCategory(category);
        t.setType("");
        t.setImpact("");
        t.setPriority(priority);
        t.setStatus(status);
        t.setCreatedAt(now.minusDays(id));
        t.setLastUpdate(now);
        t.setBranch(new Branch(location.getIdLocation(), location.getName()));
        t.setPeopleInCharge(operatorId);
        t.setCommentary(new ArrayList<>());
        tickets.save(t);
    }
}
