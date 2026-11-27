package com.panalsuite.tickets.controller;

import com.panalsuite.tickets.dto.*;
import com.panalsuite.tickets.model.Ticket;
import com.panalsuite.tickets.model.TicketStatus;
import com.panalsuite.tickets.model.Urgency;
import com.panalsuite.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    // Ej: /api/tickets?userId=1&status=New,Queued&priority=High&q=printer&from=2026-10-01
    @GetMapping
    public List<Ticket> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) List<TicketStatus> status,
            @RequestParam(required = false) List<Urgency> priority,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.search(userId, assignedTo, status, priority, q, from, to);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }

    @GetMapping("/{id}")
    public Ticket get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket create(@Valid @RequestBody TicketCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Ticket update(@PathVariable Long id, @Valid @RequestBody TicketUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/status")
    public Ticket changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.changeStatus(id, request);
    }

    @PatchMapping("/{id}/assign")
    public Ticket assign(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return service.assign(id, request);
    }

    @PatchMapping("/release")
    public List<Ticket> release(@Valid @RequestBody ReleaseRequest request) {
        return service.releaseTicketsOf(request);
    }

    @PatchMapping("/{id}/pin")
    public Ticket pin(@PathVariable Long id, @RequestBody PinRequest request) {
        return service.setPinned(id, request.pinned());
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket comment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return service.addComment(id, request);
    }
}
