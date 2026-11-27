package com.example.panalsuite.remote

import com.example.panalsuite.model.Commentary
import com.example.panalsuite.model.DevelopmentHistory
import com.example.panalsuite.model.Evidences
import com.example.panalsuite.model.Location
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.Team
import com.example.panalsuite.model.Ticket
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.Urgency
import com.example.panalsuite.model.User
import com.example.panalsuite.remote.dto.TeamDto
import com.example.panalsuite.remote.dto.TicketDto
import com.example.panalsuite.remote.dto.UserDto
import java.time.LocalDateTime

// Convierte lo que responde el backend (DTO) en los modelos que usa la interfaz.

private fun parseDate(value: String?): LocalDateTime =
    runCatching { LocalDateTime.parse(value) }.getOrElse { LocalDateTime.now() }

// El backend guarda el estado por su nombre interno ("InProgress"); la interfaz muestra "In Progress"
private fun statusLabel(raw: String?): String =
    TicketStatus.entries.firstOrNull { it.name == raw }?.StatusName ?: raw.orEmpty()

// El backend nunca envia la contraseña, por eso queda vacia en el modelo local
fun UserDto.toModel(): User = User(
    idUser = idUser,
    name = name,
    lastName = lastName,
    email = email,
    password = "",
    role = Role.entries.firstOrNull { it.name == role } ?: Role.CommonUser,
    active = active
)

fun TeamDto.toModel(): Team = Team(idTeam = idTeam, name = name, memberIds = memberIds.orEmpty())

fun TicketDto.toModel(): Ticket = Ticket(
    idTicket = idTicket,
    idUser = idUser,
    title = title,
    description = description.orEmpty(),
    category = category.orEmpty(),
    type = type.orEmpty(),
    priority = Urgency.entries.firstOrNull { it.name == priority } ?: Urgency.Low,
    impact = impact.orEmpty(),
    status = TicketStatus.entries.firstOrNull { it.name == status } ?: TicketStatus.New,
    createdAt = parseDate(createdAt),
    lastUpdate = parseDate(lastUpdate),
    branch = branch?.let { Location(it.idLocation, it.name) } ?: Location(0L, ""),
    team = team,
    peopleInCharge = peopleInCharge,
    commentary = commentary.orEmpty().map {
        Commentary(
            idCommentary = it.idCommentary,
            idTicket = idTicket,
            idUser = it.idUser,
            text = it.text,
            commentaryDate = parseDate(it.commentaryDate)
        )
    }.toMutableList(),
    developmentHistory = developmentHistory.orEmpty().map {
        val isStatus = it.modifiedField == "Status"
        DevelopmentHistory(
            idDevelopment = it.idDevelopment,
            modifiedField = it.modifiedField.orEmpty(),
            previousValue = if (isStatus) statusLabel(it.previousValue) else it.previousValue.orEmpty(),
            newValue = if (isStatus) statusLabel(it.newValue) else it.newValue.orEmpty(),
            idUser = it.idUser,
            date = parseDate(it.date)
        )
    }.toMutableList(),
    evidences = evidences.orEmpty().map {
        Evidences(
            idEvidence = it.idEvidence,
            idTicket = idTicket,
            idUser = it.idUser,
            url = it.url.orEmpty(),
            description = it.description,
            creationDate = parseDate(it.creationDate)
        )
    }.toMutableList(),
    pinned = pinned
)
