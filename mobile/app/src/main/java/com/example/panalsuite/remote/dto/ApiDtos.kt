package com.example.panalsuite.remote.dto

import com.google.gson.annotations.SerializedName

// DTOs = forma exacta del JSON que envian/reciben los microservicios.
// Las fechas llegan como texto ISO (ej. "2026-11-26T15:04:05") y se convierten en Mappers.kt.

// ---------- users-service ----------
data class LoginRequestDto(val identifier: String, val password: String)

data class UserDto(
    val idUser: Long,
    val name: String,
    val lastName: String,
    val email: String,
    val role: String,
    val active: Boolean
)

data class UserRequestDto(
    val name: String,
    val lastName: String,
    val email: String?,
    val password: String?,
    val role: String,
    val active: Boolean?
)

data class TeamDto(val idTeam: Long, val name: String, val memberIds: List<Long>?)

data class TeamRequestDto(val name: String, val memberIds: List<Long>)

// ---------- tickets-service ----------
data class BranchDto(val idLocation: Long, val name: String)

data class CommentaryDto(
    val idCommentary: Long,
    val idUser: Long,
    val text: String,
    val commentaryDate: String?
)

data class DevelopmentEntryDto(
    val idDevelopment: Long,
    val modifiedField: String?,
    val previousValue: String?,
    val newValue: String?,
    val idUser: Long,
    val date: String?
)

data class EvidenceDto(
    val idEvidence: Long,
    val idUser: Long,
    val url: String?,
    val description: String?,
    val creationDate: String?
)

data class TicketDto(
    val idTicket: Long,
    val idUser: Long,
    val title: String,
    val description: String?,
    val category: String?,
    val type: String?,
    val priority: String?,
    val impact: String?,
    val status: String?,
    val createdAt: String?,
    val lastUpdate: String?,
    val branch: BranchDto?,
    val team: String?,
    val peopleInCharge: Long?,
    val pinned: Boolean,
    val commentary: List<CommentaryDto>?,
    val developmentHistory: List<DevelopmentEntryDto>?,
    val evidences: List<EvidenceDto>?
)

data class TicketCreateRequestDto(
    val idUser: Long,
    val title: String,
    val category: String,
    val priority: String,
    val branchId: Long,
    val description: String
)

data class StatusRequestDto(val status: String, val actorId: Long)

data class AssignRequestDto(
    val operatorId: Long,
    val operatorName: String?,
    val previousOperatorName: String?,
    val actorId: Long
)

data class ReleaseRequestDto(val operatorId: Long, val operatorName: String?, val actorId: Long)

data class PinRequestDto(val pinned: Boolean)

data class CommentRequestDto(val authorId: Long, val text: String)

data class NameRequestDto(val name: String)

data class CategoryDto(val idCategory: Long, val name: String)

data class LocationDto(val idLocation: Long, val name: String)

// ---------- API externa: Open-Meteo ----------
data class WeatherResponseDto(val current: CurrentWeatherDto?)

data class CurrentWeatherDto(
    val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?
)
