package com.example.panalsuite.model

import java.time.LocalDateTime

data class Commentary (
    val idCommentary: Long,
    val idTicket: Long,
    val idUser: Long,
    val text: String,
    val commentaryDate: LocalDateTime
)