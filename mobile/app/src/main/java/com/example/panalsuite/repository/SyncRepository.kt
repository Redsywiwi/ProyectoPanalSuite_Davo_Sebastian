package com.example.panalsuite.repository

import com.example.panalsuite.model.Location
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.remote.NetworkModule
import com.example.panalsuite.remote.apiCall
import com.example.panalsuite.remote.toModel

// Descarga los datos de los microservicios y los deja en los repositorios locales.
// Lo local funciona como cache: la interfaz sigue leyendo de aqui y Persistence lo guarda en el telefono.
object SyncRepository {

    suspend fun refreshAll(): ApiResult<Unit> {
        val users = refreshUsers()
        // Si no hay conexion con un servicio no se insiste con sus otras consultas (evita esperar timeouts de mas)
        val teams = if (users is ApiResult.Failure && users.code == null) users else refreshTeams()
        val parameters = refreshParameters()
        val tickets = if (parameters is ApiResult.Failure && parameters.code == null) parameters else refreshTickets()
        return listOf(users, teams, parameters, tickets)
            .filterIsInstance<ApiResult.Failure>()
            .firstOrNull() ?: ApiResult.Success(Unit)
    }

    suspend fun refreshUsers(): ApiResult<Unit> =
        when (val result = apiCall { NetworkModule.usersApi.getUsers() }) {
            is ApiResult.Success -> {
                val users = result.data.map { it.toModel() }
                UserRepository.replaceAll(users)
                // Si cambiaron los datos del usuario con sesion iniciada, se actualiza la sesion
                Session.currentUser?.let { current ->
                    users.firstOrNull { it.idUser == current.idUser }?.let { Session.currentUser = it }
                }
                ApiResult.Success(Unit)
            }
            is ApiResult.Failure -> result
        }

    suspend fun refreshTeams(): ApiResult<Unit> =
        when (val result = apiCall { NetworkModule.usersApi.getTeams() }) {
            is ApiResult.Success -> {
                TeamRepository.restore(result.data.map { it.toModel() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Failure -> result
        }

    suspend fun refreshParameters(): ApiResult<Unit> {
        val api = NetworkModule.ticketsApi
        val categories = when (val result = apiCall { api.getCategories() }) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> return result
        }
        val locations = when (val result = apiCall { api.getLocations() }) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> return result
        }
        ParametersRepository.restore(
            categories.map { it.name },
            locations.map { Location(it.idLocation, it.name) }
        )
        return ApiResult.Success(Unit)
    }

    suspend fun refreshTickets(): ApiResult<Unit> =
        when (val result = apiCall { NetworkModule.ticketsApi.getTickets() }) {
            is ApiResult.Success -> {
                TicketRepository.restore(result.data.map { it.toModel() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Failure -> result
        }
}
