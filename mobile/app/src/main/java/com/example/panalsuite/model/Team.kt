package com.example.panalsuite.model

data class Team(
    val idTeam: Long,
    val name: String,
    val memberIds: List<Long>
)
