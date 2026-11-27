package com.panalsuite.users.dto;

import jakarta.validation.constraints.NotBlank;

// identifier = correo o id numerico (igual que la app actual)
public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {}
