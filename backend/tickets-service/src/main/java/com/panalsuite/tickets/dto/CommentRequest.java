package com.panalsuite.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(@NotNull Long authorId, @NotBlank @Size(max = 1000) String text) {}
