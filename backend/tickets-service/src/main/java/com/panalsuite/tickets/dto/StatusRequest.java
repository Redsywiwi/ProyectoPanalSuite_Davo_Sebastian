package com.panalsuite.tickets.dto;

import com.panalsuite.tickets.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull TicketStatus status, @NotNull Long actorId) {}
