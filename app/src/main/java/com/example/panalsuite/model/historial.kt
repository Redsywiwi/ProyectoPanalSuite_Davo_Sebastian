package com.example.panalsuite.model

import java.time.LocalDateTime

data class historial (
    val idHistorial: Long,
    val campoModificado : String,
    val valorAnterior : String,
    val valorNuevo : String,
    val usuario : Long,
    val fechaCambio : LocalDateTime
)