package com.example.panalsuite.remote.api

import com.example.panalsuite.remote.dto.LoginRequestDto
import com.example.panalsuite.remote.dto.TeamDto
import com.example.panalsuite.remote.dto.TeamRequestDto
import com.example.panalsuite.remote.dto.UserDto
import com.example.panalsuite.remote.dto.UserRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Microservicio propio: users-service (puerto 8081)
interface UsersApiService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): UserDto

    @GET("api/users")
    suspend fun getUsers(): List<UserDto>

    @POST("api/users")
    suspend fun createUser(@Body body: UserRequestDto): UserDto

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body body: UserRequestDto): UserDto

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long)

    @GET("api/teams")
    suspend fun getTeams(): List<TeamDto>

    @POST("api/teams")
    suspend fun createTeam(@Body body: TeamRequestDto): TeamDto

    @PUT("api/teams/{id}")
    suspend fun updateTeam(@Path("id") id: Long, @Body body: TeamRequestDto): TeamDto

    @DELETE("api/teams/{id}")
    suspend fun deleteTeam(@Path("id") id: Long)
}
