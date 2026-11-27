package com.panalsuite.tickets.model;

import java.time.LocalDateTime;

public record Evidence(Long idEvidence, Long idUser, String url, String description, LocalDateTime creationDate) {}
