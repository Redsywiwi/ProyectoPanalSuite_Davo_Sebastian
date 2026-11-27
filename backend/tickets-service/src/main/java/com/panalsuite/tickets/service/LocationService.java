package com.panalsuite.tickets.service;

import com.panalsuite.tickets.dto.NameRequest;
import com.panalsuite.tickets.exception.ApiException;
import com.panalsuite.tickets.model.Location;
import com.panalsuite.tickets.repository.LocationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    private final LocationRepository repository;
    private final SequenceService sequences;

    public LocationService(LocationRepository repository, SequenceService sequences) {
        this.repository = repository;
        this.sequences = sequences;
    }

    public List<Location> findAll() {
        return repository.findAll(Sort.by("idLocation"));
    }

    public Location create(NameRequest req) {
        String name = req.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe esa sucursal");
        }
        return repository.save(new Location(sequences.next("locations"), name));
    }

    public Location rename(Long id, NameRequest req) {
        Location item = find(id);
        String name = req.name().trim();
        repository.findByNameIgnoreCase(name)
                .filter(other -> !other.getIdLocation().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ya existe esa sucursal");
                });
        item.setName(name);
        return repository.save(item);
    }

    public void delete(Long id) {
        Location item = find(id);
        if (repository.count() <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "Debe quedar al menos una sucursal");
        }
        repository.delete(item);
    }

    private Location find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Sucursal no encontrada: " + id));
    }
}
