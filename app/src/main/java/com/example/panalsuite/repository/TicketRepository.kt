package com.example.panalsuite.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.panalsuite.model.Location
import com.example.panalsuite.model.Ticket
import com.example.panalsuite.model.TicketFilters
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.Urgency
import java.time.LocalDateTime


//Los tickets creados se borran si cuando cierras la app upsi

object TicketRepository {
    private val tickets = mutableStateListOf<Ticket>()
    private var nextTicketId = 1L

    @Synchronized
    fun createTicket(
        idUser: Long,
        title: String,
        category: String,
        priority: Urgency,
        branch: Location,
        description: String
    ): Ticket {
        val now = LocalDateTime.now()
        val ticket = Ticket(
            idTicket = nextTicketId++,
            idUser = idUser,
            title = title.trim(),
            description = description.trim(),
            category = category.trim(),
            type = "",
            priority = priority,
            impact = "",
            status = TicketStatus.New,
            createdAt = now,
            lastUpdate = now,
            branch = branch,
            team = null,
            peopleInCharge = null,
            commentary = mutableListOf(),
            developmentHistory = mutableListOf(),
            evidences = mutableListOf(),
            pinned = false
        )
        tickets.add(ticket)
        return ticket
    }

    // El ticket que se muestra primero es el recien creado o el ultimo
    fun getTicketsByUser(idUser: Long): List<Ticket> =
        tickets.filter { it.idUser == idUser }.sortedByDescending { it.idTicket }

    // Busca por texto (titulo o descripcion) y filtra por estado, prioridad y fecha de creacion.
    // Siempre se limita a los tickets del idUser recibido, nunca devuelve tickets de otros usuarios.
    fun searchTicketsByUser(idUser: Long, filters: TicketFilters): List<Ticket> {
        val text = filters.query.trim()
        val a = filters.fromDate
        val b = filters.toDate
        // Si el usuario puso las fechas al reves, se invierten
        val (from, to) = if (a != null && b != null && a.isAfter(b)) b to a else a to b

        return tickets
            .filter { it.idUser == idUser }
            .filter { ticket ->
                val matchesText = text.isEmpty() ||
                        ticket.title.contains(text, ignoreCase = true) ||
                        ticket.description.contains(text, ignoreCase = true)
                val matchesStatus = filters.statuses.isEmpty() || ticket.status in filters.statuses
                val matchesPriority = filters.priorities.isEmpty() || ticket.priority in filters.priorities
                val createdDate = ticket.createdAt.toLocalDate()
                val matchesFrom = from == null || !createdDate.isBefore(from)
                val matchesTo = to == null || !createdDate.isAfter(to)
                matchesText && matchesStatus && matchesPriority && matchesFrom && matchesTo
            }
            .sortedByDescending { it.idTicket }
    }

    // Tickets fijados
    fun getPinnedTicketsByUser(idUser: Long): List<Ticket> =
        tickets.filter { it.idUser == idUser && it.pinned }.sortedByDescending { it.idTicket }

    // Solo devuelve un ticket si coincide con el id de usuario actual
    fun getTicketForUser(idTicket: Long, idUser: Long): Ticket? =
        tickets.firstOrNull { it.idTicket == idTicket && it.idUser == idUser }

    fun setPinned(idTicket: Long, pinned: Boolean) {
        val index = tickets.indexOfFirst { it.idTicket == idTicket }
        if (index >= 0) {
            tickets[index] = tickets[index].copy(pinned = pinned)
        }
    }
}