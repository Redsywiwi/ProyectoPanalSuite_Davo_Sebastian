package com.example.panalsuite.repository

import com.example.panalsuite.model.User
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.remote.NetworkModule
import com.example.panalsuite.remote.api.UsersApiService
import com.example.panalsuite.remote.apiCall
import com.example.panalsuite.remote.dto.LoginRequestDto
import com.example.panalsuite.remote.toModel

// Login contra users-service (POST /api/auth/login). La contraseña se valida en el servidor.
class AuthRepository(private val api: UsersApiService = NetworkModule.usersApi) {
    suspend fun login(identifier: String, password: String): ApiResult<User> =
        when (val result = apiCall { api.login(LoginRequestDto(identifier.trim(), password)) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toModel())
            is ApiResult.Failure -> result
        }
}
