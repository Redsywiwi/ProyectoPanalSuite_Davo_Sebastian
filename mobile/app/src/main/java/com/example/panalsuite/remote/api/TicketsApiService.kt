package com.example.panalsuite.remote.api

import com.example.panalsuite.remote.dto.AssignRequestDto
import com.example.panalsuite.remote.dto.CategoryDto
import com.example.panalsuite.remote.dto.CommentRequestDto
import com.example.panalsuite.remote.dto.LocationDto
import com.example.panalsuite.remote.dto.NameRequestDto
import com.example.panalsuite.remote.dto.PinRequestDto
import com.example.panalsuite.remote.dto.ReleaseRequestDto
import com.example.panalsuite.remote.dto.StatusRequestDto
import com.example.panalsuite.remote.dto.TicketCreateRequestDto
import com.example.panalsuite.remote.dto.TicketDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Microservicio propio: tickets-service (puerto 8082)
interface TicketsApiService {
    @GET("api/tickets")
    suspend fun getTickets(): List<TicketDto>

    @POST("api/tickets")
    suspend fun createTicket(@Body body: TicketCreateRequestDto): TicketDto

    @PATCH("api/tickets/{id}/status")
    suspend fun changeStatus(@Path("id") id: Long, @Body body: StatusRequestDto): TicketDto

    @PATCH("api/tickets/{id}/assign")
    suspend fun assignTicket(@Path("id") id: Long, @Body body: AssignRequestDto): TicketDto

    @PATCH("api/tickets/{id}/pin")
    suspend fun pinTicket(@Path("id") id: Long, @Body body: PinRequestDto): TicketDto

    @POST("api/tickets/{id}/comments")
    suspend fun addComment(@Path("id") id: Long, @Body body: CommentRequestDto): TicketDto

    @PATCH("api/tickets/release")
    suspend fun releaseTickets(@Body body: ReleaseRequestDto): List<TicketDto>

    @GET("api/categories")
    suspend fun getCategories(): List<CategoryDto>

    @POST("api/categories")
    suspend fun createCategory(@Body body: NameRequestDto): CategoryDto

    @PUT("api/categories/{id}")
    suspend fun renameCategory(@Path("id") id: Long, @Body body: NameRequestDto): CategoryDto

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)

    @GET("api/locations")
    suspend fun getLocations(): List<LocationDto>

    @POST("api/locations")
    suspend fun createLocation(@Body body: NameRequestDto): LocationDto

    @PUT("api/locations/{id}")
    suspend fun renameLocation(@Path("id") id: Long, @Body body: NameRequestDto): LocationDto

    @DELETE("api/locations/{id}")
    suspend fun deleteLocation(@Path("id") id: Long)
}
