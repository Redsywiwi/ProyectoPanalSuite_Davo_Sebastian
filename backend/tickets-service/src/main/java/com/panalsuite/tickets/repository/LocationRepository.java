package com.panalsuite.tickets.repository;

import com.panalsuite.tickets.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LocationRepository extends MongoRepository<Location, Long> {
boolean existsByNameIgnoreCase(String name);
Optional<Location> findByNameIgnoreCase(String name);
}
