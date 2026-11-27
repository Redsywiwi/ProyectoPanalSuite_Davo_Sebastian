package com.example.panalsuite.repository

import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.User
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.remote.NetworkModule
import com.example.panalsuite.remote.apiCall
import com.example.panalsuite.remote.dto.TeamRequestDto

// CRUD de equipos contra users-service (/api/teams)
object TeamRemoteRepository {
    private val api get() = NetworkModule.usersApi

    suspend fun create(actor: User, name: String, memberIds: List<Long>): ApiResult<Unit> =
        write(actor) { api.createTeam(TeamRequestDto(name.trim(), memberIds.distinct())) }

    suspend fun update(actor: User, idTeam: Long, name: String, memberIds: List<Long>): ApiResult<Unit> =
        write(actor) { api.updateTeam(idTeam, TeamRequestDto(name.trim(), memberIds.distinct())) }

    suspend fun delete(actor: User, idTeam: Long): ApiResult<Unit> =
        write(actor) { api.deleteTeam(idTeam) }

    // Revisa el permiso, ejecuta la llamada y, si salio bien, vuelve a descargar los equipos
    private suspend fun write(actor: User, call: suspend () -> Any?): ApiResult<Unit> {
        if (Permissions.ManageTeams !in actor.role.permits) {
            return ApiResult.Failure("No tienes permiso para gestionar equipos", 403)
        }
        return when (val result = apiCall { call() }) {
            is ApiResult.Success -> SyncRepository.refreshTeams()
            is ApiResult.Failure -> result
        }
    }
}
