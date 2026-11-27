package com.example.panalsuite.remote

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

// Resultado de una llamada de red: o hay datos, o hay un mensaje de error listo para mostrar.
// code == null significa que no hubo respuesta del servidor (sin conexion, timeout, etc.).
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val message: String, val code: Int? = null) : ApiResult<Nothing>
}

const val NO_CONNECTION_MESSAGE = "No se pudo conectar con el servidor"

// Ejecuta una llamada Retrofit y traduce las excepciones a ApiResult.Failure
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        ApiResult.Failure(serverMessage(e), e.code())
    } catch (e: IOException) {
        ApiResult.Failure(NO_CONNECTION_MESSAGE)
    } catch (e: Exception) {
        ApiResult.Failure("Respuesta inesperada del servidor", 0)
    }

// Los microservicios responden los errores como { status, error, message }
private fun serverMessage(e: HttpException): String {
    val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    val message = raw?.let {
        runCatching { JsonParser.parseString(it).asJsonObject.get("message")?.asString }.getOrNull()
    }
    return message ?: "Error del servidor (${e.code()})"
}
