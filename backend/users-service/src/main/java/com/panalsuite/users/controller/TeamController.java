package com.panalsuite.users.controller;

import com.panalsuite.users.dto.TeamRequest;
import com.panalsuite.users.model.Team;
import com.panalsuite.users.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @GetMapping
    public List<Team> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Team get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team create(@Valid @RequestBody TeamRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Team update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
