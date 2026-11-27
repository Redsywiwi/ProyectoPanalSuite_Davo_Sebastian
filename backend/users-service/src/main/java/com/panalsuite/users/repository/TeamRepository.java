package com.panalsuite.users.repository;

import com.panalsuite.users.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TeamRepository extends MongoRepository<Team, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Team> findByNameIgnoreCase(String name);
}
