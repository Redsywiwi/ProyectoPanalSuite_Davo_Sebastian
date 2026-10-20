package com.example.panalsuite.model

import java.time.LocalDateTime

data class Ticket (
    val idTicket : Long,
    val idUser :  Long, //ids de usuario
    val title: String,
    val description : String,
    val category: String,
    val type: String,
    var priority: Urgency,// siento que string no es lo mejor que se pueda usar || edit: No lo era
    val impact: String,
    var status: TicketStatus,  // y esto
    val createdAt: LocalDateTime,
    var lastUpdate: LocalDateTime,
    val branch : Location, //arreglao gemelo wijiji
    var team : String?,//El ? es porque se puede dejar null
    var peopleInCharge : Long?,
    val commentary : MutableList<Commentary>,
    val developmentHistory: MutableList<DevelopmentHistory>,
    val evidences : MutableList<Evidences>,
    val pinned: Boolean = false

)