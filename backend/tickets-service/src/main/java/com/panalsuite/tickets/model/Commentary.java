package com.panalsuite.tickets.model;

import java.time.LocalDateTime;

public record Commentary(Long idCommentary, Long idUser, String text, LocalDateTime commentaryDate) {}
