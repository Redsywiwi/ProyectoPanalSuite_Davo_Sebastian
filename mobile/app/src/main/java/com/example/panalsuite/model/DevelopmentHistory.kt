package com.example.panalsuite.model

import java.time.LocalDateTime

data class DevelopmentHistory (
    val idDevelopment: Long,
    val modifiedField : String,
    val previousValue : String,
    val newValue : String,
    val idUser : Long,
    val date : LocalDateTime
)