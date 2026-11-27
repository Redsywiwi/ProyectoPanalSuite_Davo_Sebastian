package com.panalsuite.tickets.model;

import java.time.LocalDateTime;

// Una linea del historial: que campo cambio, de que a que, quien y cuando
public record DevelopmentEntry(Long idDevelopment, String modifiedField, String previousValue,
                               String newValue, Long idUser, LocalDateTime date) {}
