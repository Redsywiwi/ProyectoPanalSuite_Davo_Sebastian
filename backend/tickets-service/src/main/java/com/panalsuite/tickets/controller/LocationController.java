package com.panalsuite.tickets.controller;

import com.panalsuite.tickets.dto.NameRequest;
import com.panalsuite.tickets.model.Location;
import com.panalsuite.tickets.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Location> list() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location create(@Valid @RequestBody NameRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Location rename(@PathVariable Long id, @Valid @RequestBody NameRequest request) {
        return service.rename(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
