package com.example.panalsuite.model

import java.time.LocalDateTime

data class Ticket (
    val idTicket : Long,
    val idUser :  Long, //ids de usuario
    val title: String,
    val description : String,
    val category: String,
    val type: String,
    var priority: String,// siento q string no es lo mejor que se pueda usar
    val impact: String, // lo mismo digo con esto
    var status: String,  // y esto
    val createdAt: LocalDateTime,
    var lastUpdate: LocalDateTime,
    val branch : String, //no estoy seguro si la sucursal puede cambiar asi que la dejo que no por default
    var team : String?,//El ? es porque se puede dejar null
    var peopleInCharge : Long?,
    val commentary : MutableList<Commentary>,
    val developmentHistory: MutableList<DevelopmentHistory>,
    val evidences : MutableList<Evidences>

)