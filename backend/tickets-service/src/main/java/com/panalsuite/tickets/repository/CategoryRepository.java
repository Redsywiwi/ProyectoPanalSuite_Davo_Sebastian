package com.panalsuite.tickets.repository;

import com.panalsuite.tickets.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, Long> {
boolean existsByNameIgnoreCase(String name);
Optional<Category> findByNameIgnoreCase(String name);
}
