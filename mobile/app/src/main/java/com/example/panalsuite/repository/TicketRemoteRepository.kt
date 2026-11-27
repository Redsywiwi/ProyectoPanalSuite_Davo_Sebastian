package com.example.panalsuite.repository

import com.example.panalsuite.model.Location
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.Ticket
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.Urgency
import com.example.panalsuite.model.User
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.remote.NetworkModule
import com.example.panalsuite.remote.apiCall
import com.example.panalsuite.remote.dto.AssignRequestDto
import com.example.panalsuite.remote.dto.CommentRequestDto
import com.example.panalsuite.remote.dto.PinRequestDto
import com.example.panalsuite.remote.dto.ReleaseRequestDto
import com.example.panalsuite.remote.dto.StatusRequestDto
import com.example.panalsuite.remote.dto.TicketCreateRequestDto
import com.example.panalsuite.remote.dto.TicketDto
import com.example.panalsuite.remote.toModel

// Operaciones de tickets contra tickets-service. Primero se escribe en el servidor y, si resulta bien,
// el ticket que devuelve el servidor reemplaza al local (el servidor es la fuente de verdad).
object TicketRemoteRepository {
    private val api get() = NetworkModule.ticketsApi

    suspend fun create(
        idUser: Long,
        title: String,
        category: String,
        priority: Urgency,
        branch: Location,
        description: String
    ): ApiResult<Ticket> = mirror(
        apiCall {
            api.createTicket(
                TicketCreateRequestDto(
                    idUser = idUser,
                    title = title.trim(),
                    category = category.trim(),
                    priority = priority.name,
                    branchId = branch.idLocation,
                    description = description.trim()
                )
            )
        }
    )

    suspend fun changeStatus(idTicket: Long, newStatus: TicketStatus, actor: User): ApiResult<Ticket> {
        if (Permissions.ChangeStatus !in actor.role.permits) return denied()
        return mirror(apiCall { api.changeStatus(idTicket, StatusRequestDto(newStatus.name, actor.idUser)) })
    }

    suspend fun assign(
        idTicket: Long,
        target: User,
        actor: User,
        previousAssigneeName: String?
    ): ApiResult<Ticket> {
        if (Permissions.Reassign !in actor.role.permits) return denied()
        if (target.role != Role.Operator || !target.active) {
            return ApiResult.Failure("El usuario elegido no es un operador activo", 400)
        }
        val body = AssignRequestDto(
            operatorId = target.idUser,
            operatorName = "${target.name} ${target.lastName}",
            previousOperatorName = previousAssigneeName,
            actorId = actor.idUser
        )
        return mirror(apiCall { api.assignTicket(idTicket, body) })
    }

    suspend fun addComment(idTicket: Long, author: User, text: String): ApiResult<Ticket> {
        if (Permissions.Comment !in author.role.permits) return denied()
        val clean = text.trim()
        if (clean.isEmpty()) return ApiResult.Failure("El comentario está vacío", 400)
        return mirror(apiCall { api.addComment(idTicket, CommentRequestDto(author.idUser, clean)) })
    }

    suspend fun setPinned(idTicket: Long, pinned: Boolean): ApiResult<Ticket> =
        mirror(apiCall { api.pinTicket(idTicket, PinRequestDto(pinned)) })

    // Deja sin asignar los tickets de un operador (cuando se elimina, se desactiva o cambia de rol)
    suspend fun releaseTicketsOf(idOperator: Long, operatorName: String, actor: User): ApiResult<Unit> =
        when (val result = apiCall {
            api.releaseTickets(ReleaseRequestDto(idOperator, operatorName, actor.idUser))
        }) {
            is ApiResult.Success -> {
                result.data.forEach { TicketRepository.upsert(it.toModel()) }
                ApiResult.Success(Unit)
            }
            is ApiResult.Failure -> result
        }

    private fun mirror(result: ApiResult<TicketDto>): ApiResult<Ticket> =
        when (result) {
            is ApiResult.Success -> {
                val ticket = result.data.toModel()
                TicketRepository.upsert(ticket)
                ApiResult.Success(ticket)
            }
            is ApiResult.Failure -> result
        }

    private fun denied() = ApiResult.Failure("No tienes permiso para realizar esta acción", 403)
}
