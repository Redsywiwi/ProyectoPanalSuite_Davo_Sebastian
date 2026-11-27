package com.example.panalsuite.model

import java.time.LocalDateTime

data class Evidences(
    val idEvidence: Long,
    val idTicket: Long,
    val idUser: Long,
    val url: String,
    val description: String?,
    val creationDate: LocalDateTime
)