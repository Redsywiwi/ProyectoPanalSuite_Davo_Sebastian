package com.example.panalsuite.repository

import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.remote.NetworkModule
import com.example.panalsuite.remote.api.WeatherApiService
import com.example.panalsuite.remote.apiCall

data class WeatherInfo(
    val temperature: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val description: String
)

// Traduce el codigo WMO que entrega Open-Meteo a un texto en español
fun describeWeatherCode(code: Int): String = when (code) {
    0 -> "Despejado"
    1 -> "Mayormente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    in 51..57 -> "Llovizna"
    in 61..67 -> "Lluvia"
    in 71..77 -> "Nieve"
    in 80..82 -> "Chubascos"
    85, 86 -> "Chubascos de nieve"
    in 95..99 -> "Tormenta"
    else -> "Sin información"
}

// API EXTERNA: clima actual desde Open-Meteo (sin clave). No toca los datos locales ni los microservicios.
class WeatherRepository(private val api: WeatherApiService = NetworkModule.weatherApi) {

    suspend fun getCurrentWeather(latitude: Double, longitude: Double): ApiResult<WeatherInfo> =
        when (val result = apiCall { api.getCurrentWeather(latitude, longitude, CURRENT_FIELDS, "auto") }) {
            is ApiResult.Success -> {
                val current = result.data.current
                val temperature = current?.temperature
                if (current == null || temperature == null) {
                    ApiResult.Failure("El servicio del clima no entregó datos", 0)
                } else {
                    val code = current.weatherCode ?: -1
                    ApiResult.Success(
                        WeatherInfo(
                            temperature = temperature,
                            windSpeed = current.windSpeed ?: 0.0,
                            weatherCode = code,
                            description = describeWeatherCode(code)
                        )
                    )
                }
            }
            is ApiResult.Failure -> result
        }

    private companion object {
        const val CURRENT_FIELDS = "temperature_2m,weather_code,wind_speed_10m"
    }
}
