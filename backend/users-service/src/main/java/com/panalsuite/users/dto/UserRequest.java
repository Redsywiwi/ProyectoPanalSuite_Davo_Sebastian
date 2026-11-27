package com.panalsuite.users.dto;

import com.panalsuite.users.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// password es obligatorio al crear; al actualizar, si viene vacio se conserva la actual
public record UserRequest(
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Size(max = 60) String lastName,
        @Email String email,
        @Size(max = 100) String password,
        @NotNull Role role,
        Boolean active) {}
