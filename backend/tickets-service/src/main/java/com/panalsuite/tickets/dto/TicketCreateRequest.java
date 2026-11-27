package com.panalsuite.tickets.dto;

import com.panalsuite.tickets.model.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
        @NotNull Long idUser,
        @NotBlank @Size(min = 3, max = 120) String title,
        @NotBlank String category,
        @NotNull Urgency priority,
        @NotNull Long branchId,
        @NotBlank @Size(max = 2000) String description) {}
