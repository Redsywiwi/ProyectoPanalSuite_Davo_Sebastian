package com.panalsuite.tickets.dto;

import jakarta.validation.constraints.NotNull;

// operatorName y previousOperatorName se guardan en el historial para que sea legible
public record AssignRequest(@NotNull Long operatorId, String operatorName, String previousOperatorName,
                            @NotNull Long actorId) {}
