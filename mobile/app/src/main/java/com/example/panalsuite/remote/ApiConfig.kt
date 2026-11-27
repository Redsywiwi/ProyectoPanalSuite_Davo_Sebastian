package com.example.panalsuite.remote

// Direcciones base de los servicios.
// - Emulador de Android Studio: 10.0.2.2 apunta al PC donde corren los microservicios.
// - Celular fisico: cambiar HOST por la IP del PC en la red local (ej. "192.168.1.50").
object ApiConfig {
    private const val HOST = "10.0.2.2"

    const val USERS_BASE_URL = "http://$HOST:8081/"
    const val TICKETS_BASE_URL = "http://$HOST:8082/"

    // API externa (Open-Meteo): gratuita, sin clave, solo para uso no comercial
    const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
}
