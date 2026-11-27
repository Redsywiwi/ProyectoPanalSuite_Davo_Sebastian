package com.example.panalsuite.remote.api

import com.example.panalsuite.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

// API EXTERNA (Open-Meteo): no es nuestra, no usa base de datos propia ni se mezcla con los microservicios.
interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("timezone") timezone: String
    ): WeatherResponseDto
}
