package com.panalsuite.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TeamRequest(@NotBlank @Size(max = 60) String name, List<Long> memberIds) {}
