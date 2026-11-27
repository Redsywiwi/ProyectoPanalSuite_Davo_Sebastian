package com.example.panalsuite.ui.components

import android.content.Context
import android.widget.Toast
import com.example.panalsuite.remote.ApiResult

// Muestra el mensaje de error de una llamada de red (si la llamada salio bien no hace nada)
fun ApiResult<*>.toastIfFailure(context: Context) {
    if (this is ApiResult.Failure) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
