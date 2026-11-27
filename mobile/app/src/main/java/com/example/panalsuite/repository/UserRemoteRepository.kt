package com.example.panalsuite.repository

import android.content.Context
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.User
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.remote.NetworkModule
import com.example.panalsuite.remote.api.UsersApiService
import com.example.panalsuite.remote.apiCall
import com.example.panalsuite.remote.dto.UserRequestDto

// CRUD de usuarios contra users-service. Las reglas simples (permisos, campos vacios, no modificarse a si mismo)
// se revisan antes de llamar al servidor; las demas (correo repetido, ultimo administrador) las decide el servidor.
class UserRemoteRepository(
    context: Context,
    private val api: UsersApiService = NetworkModule.usersApi
) {
    private val local = UserRepository(context)

    suspend fun create(
        actor: User,
        name: String,
        lastName: String,
        email: String,
        password: String,
        role: Role
    ): UserError? {
        if (Permissions.ManageUsers !in actor.role.permits) return UserError.NotAllowed
        if (name.isBlank() || lastName.isBlank() || password.isEmpty()) return UserError.MissingFields
        val body = UserRequestDto(name.trim(), lastName.trim(), email.trim().ifEmpty { null }, password, role.name, true)
        return when (val result = apiCall { api.createUser(body) }) {
            is ApiResult.Success -> {
                SyncRepository.refreshUsers()
                null
            }
            is ApiResult.Failure -> result.toUserError()
        }
    }

    suspend fun update(
        actor: User,
        idUser: Long,
        name: String,
        lastName: String,
        email: String,
        password: String,
        role: Role,
        active: Boolean
    ): UserError? {
        if (Permissions.ManageUsers !in actor.role.permits) return UserError.NotAllowed
        // Al editar, la contraseña vacia significa "conservar la actual"
        if (name.isBlank() || lastName.isBlank()) return UserError.MissingFields
        val previous = local.getUsers().firstOrNull { it.idUser == idUser } ?: return UserError.NotFound
        if (idUser == actor.idUser && (role != previous.role || !active)) return UserError.CannotModifySelf

        val body = UserRequestDto(
            name.trim(), lastName.trim(), email.trim().ifEmpty { null }, password.ifEmpty { null }, role.name, active
        )
        return when (val result = apiCall { api.updateUser(idUser, body) }) {
            is ApiResult.Success -> {
                // Si deja de ser un operador activo, sus tickets quedan sin asignar
                if (previous.role == Role.Operator && (role != Role.Operator || !active)) {
                    TicketRemoteRepository.releaseTicketsOf(idUser, "${previous.name} ${previous.lastName}", actor)
                }
                SyncRepository.refreshUsers()
                null
            }
            is ApiResult.Failure -> result.toUserError()
        }
    }

    suspend fun delete(actor: User, idUser: Long): UserError? {
        if (Permissions.ManageUsers !in actor.role.permits) return UserError.NotAllowed
        val target = local.getUsers().firstOrNull { it.idUser == idUser } ?: return UserError.NotFound
        if (idUser == actor.idUser) return UserError.CannotModifySelf

        return when (val result = apiCall { api.deleteUser(idUser) }) {
            is ApiResult.Success -> {
                TicketRemoteRepository.releaseTicketsOf(idUser, "${target.name} ${target.lastName}", actor)
                SyncRepository.refreshUsers()
                null
            }
            is ApiResult.Failure -> result.toUserError()
        }
    }

    private fun ApiResult.Failure.toUserError(): UserError = when (code) {
        null -> UserError.Network
        400 -> UserError.MissingFields
        401, 403 -> UserError.NotAllowed
        404 -> UserError.NotFound
        409 -> if (message.contains("administrador", ignoreCase = true)) UserError.LastAdministrator
        else UserError.EmailTaken
        else -> UserError.Server
    }
}
