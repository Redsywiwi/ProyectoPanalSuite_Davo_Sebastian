package com.example.panalsuite.model

import java.time.LocalDateTime

data class evidencia(
    val idEvidencia: Long,
    val idTicket: Long,
    val idUsuario: Long,
    val ruta: String,
    val descripcion: String?,
    val fechaCreacion: LocalDateTime
)