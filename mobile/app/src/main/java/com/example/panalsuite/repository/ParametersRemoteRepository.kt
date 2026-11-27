package com.example.panalsuite.repository

import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.User
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.remote.NetworkModule
import com.example.panalsuite.remote.apiCall
import com.example.panalsuite.remote.dto.NameRequestDto

// CRUD de categorias y sucursales contra tickets-service (/api/categories y /api/locations)
object ParametersRemoteRepository {
    private val api get() = NetworkModule.ticketsApi

    suspend fun addCategory(actor: User, name: String): ParamError? =
        write(actor, name) { clean -> api.createCategory(NameRequestDto(clean)) }

    suspend fun renameCategory(actor: User, oldName: String, newName: String): ParamError? {
        if (Permissions.ManageParameters !in actor.role.permits) return ParamError.NotAllowed
        val id = when (val found = findCategoryId(oldName)) {
            is ApiResult.Success -> found.data ?: return ParamError.NotFound
            is ApiResult.Failure -> return found.toParamError()
        }
        return write(actor, newName) { clean -> api.renameCategory(id, NameRequestDto(clean)) }
    }

    suspend fun deleteCategory(actor: User, name: String): ParamError? {
        if (Permissions.ManageParameters !in actor.role.permits) return ParamError.NotAllowed
        val id = when (val found = findCategoryId(name)) {
            is ApiResult.Success -> found.data ?: return ParamError.NotFound
            is ApiResult.Failure -> return found.toParamError()
        }
        return write(actor, null) { api.deleteCategory(id) }
    }

    suspend fun addLocation(actor: User, name: String): ParamError? =
        write(actor, name) { clean -> api.createLocation(NameRequestDto(clean)) }

    suspend fun renameLocation(actor: User, idLocation: Long, newName: String): ParamError? =
        write(actor, newName) { clean -> api.renameLocation(idLocation, NameRequestDto(clean)) }

    suspend fun deleteLocation(actor: User, idLocation: Long): ParamError? =
        write(actor, null) { api.deleteLocation(idLocation) }

    // En la app las categorias se identifican por nombre; el servidor usa un id numerico
    private suspend fun findCategoryId(name: String): ApiResult<Long?> =
        apiCall { api.getCategories().firstOrNull { it.name == name }?.idCategory }

    // name == null para las eliminaciones (no hay nombre que validar)
    private suspend fun write(actor: User, name: String?, call: suspend (String) -> Any?): ParamError? {
        if (Permissions.ManageParameters !in actor.role.permits) return ParamError.NotAllowed
        val clean = name?.trim()
        if (clean != null && clean.isEmpty()) return ParamError.Empty
        return when (val result = apiCall { call(clean.orEmpty()) }) {
            is ApiResult.Success -> {
                SyncRepository.refreshParameters()
                null
            }
            is ApiResult.Failure -> result.toParamError()
        }
    }

    private fun ApiResult.Failure.toParamError(): ParamError = when (code) {
        null -> ParamError.Network
        400 -> ParamError.Empty
        401, 403 -> ParamError.NotAllowed
        404 -> ParamError.NotFound
        409 -> if (message.contains("al menos", ignoreCase = true)) ParamError.LastOne
        else ParamError.Duplicate
        else -> ParamError.Server
    }
}
