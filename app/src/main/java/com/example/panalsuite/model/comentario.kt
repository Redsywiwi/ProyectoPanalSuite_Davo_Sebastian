package com.example.panalsuite.model

import java.time.LocalDateTime

data class comentario (
    val idComentario: Long,
    val idUsuario: Long,
    val texto: String,
    val fechaComentario: LocalDateTime
)