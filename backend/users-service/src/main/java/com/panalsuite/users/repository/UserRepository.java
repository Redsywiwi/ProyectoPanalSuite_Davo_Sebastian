package com.panalsuite.users.repository;

import com.panalsuite.users.model.Role;
import com.panalsuite.users.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRoleAndActive(Role role, boolean active);
}
