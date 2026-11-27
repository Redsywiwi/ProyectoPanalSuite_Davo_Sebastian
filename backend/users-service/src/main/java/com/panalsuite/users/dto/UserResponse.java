package com.panalsuite.users.dto;

import com.panalsuite.users.model.Role;
import com.panalsuite.users.model.User;

// Nunca se devuelve la contrasena ni su hash
public record UserResponse(Long idUser, String name, String lastName, String email, Role role, boolean active) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getIdUser(), u.getName(), u.getLastName(), u.getEmail(), u.getRole(), u.isActive());
    }
}
