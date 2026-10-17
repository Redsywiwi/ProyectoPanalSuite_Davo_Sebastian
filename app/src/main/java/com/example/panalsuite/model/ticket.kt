package com.example.panalsuite.model

import java.time.LocalDateTime

data class ticket (
    val idTicket : Long,
    val idSolicitante : Long, //ids de usuario
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val tipo: String,
    var prioridad: String,// siento q string no es lo mejor que se pueda usar
    val impacto: String, // lo mismo digo con esto
    var estado: String,  // y esto
    val fechaCreacion: LocalDateTime,
    var fechaActualizacion: LocalDateTime,
    val sucursal : String, //no estoy seguro si la sucursal puede cambiar asi que la dejo que no por default
    var equipoResolutor : String?,
    var responsableAsignado : Long?,
    val comentarios : MutableList<comentario>,
    val historialTicket: MutableList<historial>,
    val evidencias : MutableList<evidencia>

)



//Trazabilidad de cambios e indicadores basicos de seguimiento
//creo que la trazabilidad de cambios se hace en otra parte (historial de modificaciones al ticket)
//Los indicadores de seguimiento son la fecha de creacion, actualizacion, estado y prioridad




//creo que esta funcion va en alguno de los otros cosos
//fun guardarArchivo(){ }

//van en repository

//esta creo que debe ser de aqui(? no estoy seguro
//fun leerArchivo(){}