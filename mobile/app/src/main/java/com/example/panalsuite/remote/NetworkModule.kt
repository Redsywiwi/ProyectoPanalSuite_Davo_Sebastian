package com.example.panalsuite.remote

import com.example.panalsuite.remote.api.TicketsApiService
import com.example.panalsuite.remote.api.UsersApiService
import com.example.panalsuite.remote.api.WeatherApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Construye Retrofit una sola vez y entrega un servicio por cada backend
object NetworkModule {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usersApi: UsersApiService by lazy { retrofit(ApiConfig.USERS_BASE_URL).create(UsersApiService::class.java) }
    val ticketsApi: TicketsApiService by lazy { retrofit(ApiConfig.TICKETS_BASE_URL).create(TicketsApiService::class.java) }
    val weatherApi: WeatherApiService by lazy { retrofit(ApiConfig.WEATHER_BASE_URL).create(WeatherApiService::class.java) }
}
