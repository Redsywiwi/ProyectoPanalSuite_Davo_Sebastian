package com.example.panalsuite.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.panalsuite.model.Commentary
import com.example.panalsuite.model.DashboardStats
import com.example.panalsuite.model.DevelopmentHistory
import com.example.panalsuite.model.Location
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.Ticket
import com.example.panalsuite.model.TicketFilters
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.OperatorLoad
import com.example.panalsuite.model.Urgency
import com.example.panalsuite.model.User
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


//Los tickets creados se borran si cuando cierras la app upsi

object TicketRepository {
    private val tickets = mutableStateListOf<Ticket>()
    private var nextTicketId = 1L
    private var nextCommentaryId = 1L
    private var nextDevelopmentId = 1L

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

    // Solo para usuarios con el permiso ViewSystemTray, devuelve vacio si no lo tienen.
    fun getAllTickets(viewer: User): List<Ticket> =
        if (canViewAll(viewer)) tickets.sortedByDescending { it.idTicket } else emptyList()

    // Busca por texto (titulo o descripcion) y filtra por estado, prioridad y fecha de creacion.
    fun searchTicketsByUser(idUser: Long, filters: TicketFilters): List<Ticket> =
        applyFilters(tickets.filter { it.idUser == idUser }, filters)

    // Igual que la de arriba pero sobre todos los tickets / con permiso ViewSystemTray
    fun searchAllTickets(viewer: User, filters: TicketFilters): List<Ticket> =
        if (canViewAll(viewer)) applyFilters(tickets, filters) else emptyList()

    // Tickets cuyo responsable (peopleInCharge) es el operator indicado, con busqueda y filtros.
    fun searchTicketsAssignedTo(idOperator: Long, filters: TicketFilters): List<Ticket> =
        applyFilters(tickets.filter { it.peopleInCharge == idOperator }, filters)

    private fun applyFilters(source: List<Ticket>, filters: TicketFilters): List<Ticket> {
        val text = filters.query.trim()
        val a = filters.fromDate
        val b = filters.toDate
        // Si el usuario puso las fechas al reves, se invierten
        val (from, to) = if (a != null && b != null && a.isAfter(b)) b to a else a to b

        return source
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

    // Devuelve el ticket si el usuario es el dueño, o si su rol puede ver toda la bandeja
    fun getTicketForViewer(idTicket: Long, viewer: User): Ticket? =
        tickets.firstOrNull { it.idTicket == idTicket && canView(it, viewer) }

    fun setPinned(idTicket: Long, pinned: Boolean) {
        val index = tickets.indexOfFirst { it.idTicket == idTicket }
        if (index >= 0) {
            tickets[index] = tickets[index].copy(pinned = pinned)
        }
    }
    @Synchronized
    fun changeStatus(idTicket: Long, newStatus: TicketStatus, actor: User): Boolean {
        if (Permissions.ChangeStatus !in actor.role.permits) return false
        val index = tickets.indexOfFirst { it.idTicket == idTicket }
        if (index < 0) return false
        val ticket = tickets[index]
        if (!canView(ticket, actor) || ticket.status == newStatus) return false

        val now = LocalDateTime.now()
        val entry = DevelopmentHistory(
            idDevelopment = nextDevelopmentId++,
            modifiedField = "Status",
            previousValue = ticket.status.StatusName,
            newValue = newStatus.StatusName,
            idUser = actor.idUser,
            date = now
        )
        tickets[index] = ticket.copy(
            status = newStatus,
            lastUpdate = now,
            developmentHistory = (ticket.developmentHistory + entry).toMutableList()
        )
        return true
    }

    @Synchronized
    fun assignTicket(
        idTicket: Long,
        target: User,
        actor: User,
        previousAssigneeName: String? = null // nombre del responsable anterior, para el historial
    ): Boolean {
        if (Permissions.Reassign !in actor.role.permits) return false
        if (target.role != Role.Operator || !target.active) return false
        val index = tickets.indexOfFirst { it.idTicket == idTicket }
        if (index < 0) return false
        val ticket = tickets[index]
        if (!canView(ticket, actor) || ticket.peopleInCharge == target.idUser) return false

        val now = LocalDateTime.now()
        val entry = DevelopmentHistory(
            idDevelopment = nextDevelopmentId++,
            modifiedField = "Assigned to",
            previousValue = ticket.peopleInCharge?.let { previousAssigneeName ?: "Operator #$it" } ?: "Unassigned",
            newValue = "${target.name} ${target.lastName}",
            idUser = actor.idUser,
            date = now
        )
        tickets[index] = ticket.copy(
            peopleInCharge = target.idUser,
            lastUpdate = now,
            developmentHistory = (ticket.developmentHistory + entry).toMutableList()
        )
        return true
    }
    // Deja sin asignar los tickets de un operator (cuando se borra, se desactiva o cambia de rol)
    @Synchronized
    fun releaseTicketsOf(idOperator: Long, operatorName: String, actor: User) {
        val now = LocalDateTime.now()
        for (index in tickets.indices) {
            val ticket = tickets[index]
            if (ticket.peopleInCharge != idOperator) continue
            val entry = DevelopmentHistory(
                idDevelopment = nextDevelopmentId++,
                modifiedField = "Assigned to",
                previousValue = operatorName,
                newValue = "Unassigned",
                idUser = actor.idUser,
                date = now
            )
            tickets[index] = ticket.copy(
                peopleInCharge = null,
                lastUpdate = now,
                developmentHistory = (ticket.developmentHistory + entry).toMutableList()
            )
        }
    }

    @Synchronized
    fun importTicket(
        idUser: Long,
        title: String,
        category: String,
        priority: Urgency,
        branch: Location,
        description: String,
        status: TicketStatus,
        createdAt: LocalDateTime,
        lastUpdate: LocalDateTime,
        peopleInCharge: Long?,
        comments: List<Triple<Long, String, LocalDateTime>>
    ): Ticket {
        val id = nextTicketId++
        val ticket = Ticket(
            idTicket = id,
            idUser = idUser,
            title = title.trim(),
            description = description.trim(),
            category = category.trim(),
            type = "",
            priority = priority,
            impact = "",
            status = status,
            createdAt = createdAt,
            lastUpdate = lastUpdate,
            branch = branch,
            team = null,
            peopleInCharge = peopleInCharge,
            commentary = comments.map { (author, text, date) ->
                Commentary(
                    idCommentary = nextCommentaryId++,
                    idTicket = id,
                    idUser = author,
                    text = text.trim(),
                    commentaryDate = date
                )
            }.toMutableList(),
            developmentHistory = mutableListOf(),
            evidences = mutableListOf(),
            pinned = false
        )
        tickets.add(ticket)
        return ticket
    }

    @Synchronized
    fun addComment(idTicket: Long, author: User, text: String): Boolean {
        if (Permissions.Comment !in author.role.permits) return false
        val clean = text.trim()
        if (clean.isEmpty()) return false
        val index = tickets.indexOfFirst { it.idTicket == idTicket }
        if (index < 0) return false
        val ticket = tickets[index]
        if (!canView(ticket, author)) return false

        val now = LocalDateTime.now()
        val comment = Commentary(
            idCommentary = nextCommentaryId++,
            idTicket = idTicket,
            idUser = author.idUser,
            text = clean,
            commentaryDate = now
        )
        tickets[index] = ticket.copy(
            lastUpdate = now,
            commentary = (ticket.commentary + comment).toMutableList()
        )
        return true
    }

    @Synchronized
    fun getStats(viewer: User, required: Permissions): DashboardStats? {
        if (required !in viewer.role.permits) return null
        val now = LocalDateTime.now()
        val all = tickets.toList()
        val open = all.filter { it.status != TicketStatus.Closed }
        val openAges = open.map { ChronoUnit.DAYS.between(it.createdAt, now) }
        val load = all
            .filter { it.peopleInCharge != null }
            .groupBy { it.peopleInCharge!! }
            .map { (operatorId, list) ->
                OperatorLoad(
                    operatorId = operatorId,
                    open = list.count { it.status != TicketStatus.Closed },
                    closed = list.count { it.status == TicketStatus.Closed }
                )
            }
        return DashboardStats(
            total = all.size,
            open = open.size,
            closed = all.size - open.size,
            assigned = all.count { it.peopleInCharge != null },
            openUnassigned = open.count { it.peopleInCharge == null },
            openCritical = open.count { it.priority == Urgency.Critical },
            byStatus = TicketStatus.entries.associateWith { s -> all.count { it.status == s } },
            byPriority = Urgency.entries.associateWith { p -> all.count { it.priority == p } },
            createdLast7Days = all.count { !it.createdAt.isBefore(now.minusDays(7)) },
            oldestOpenAgeDays = openAges.maxOrNull(),
            load = load
        )
    }

    private fun canViewAll(user: User) = Permissions.ViewSystemTray in user.role.permits

    private fun canView(ticket: Ticket, user: User) =
        ticket.idUser == user.idUser || canViewAll(user)
}