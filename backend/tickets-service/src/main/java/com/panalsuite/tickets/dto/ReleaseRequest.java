package com.panalsuite.tickets.dto;

import jakarta.validation.constraints.NotNull;

// Deja sin asignar todos los tickets de un operador (se elimino, se desactivo o cambio de rol)
public record ReleaseRequest(@NotNull Long operatorId, String operatorName, @NotNull Long actorId) {}
