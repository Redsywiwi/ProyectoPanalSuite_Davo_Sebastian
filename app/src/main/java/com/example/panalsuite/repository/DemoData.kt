package com.example.panalsuite.repository

import com.example.panalsuite.model.Location
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.Urgency
import java.time.Duration
import java.time.LocalDateTime

object DemoData {
    var loaded = false

    private class DemoTicket(
        val title: String,
        val category: String,
        val priority: Urgency,
        val branch: Location,
        val description: String,
        val assignedTo: Long,
        val createdAt: LocalDateTime,
        val lastUpdate: LocalDateTime,
        val comments: List<Pair<Long, String>>,
        val status: TicketStatus
    )

    private fun at(day: Int, hour: Int, minute: Int): LocalDateTime =
        LocalDateTime.of(2026, 10, day, hour, minute)

    private val estacionCentral = Location(1, "Estación Central")
    private val plazaEgana = Location(2, "Plaza Egaña")
    private val tobalaba = Location(3, "Tobalaba")
    private val manquehue = Location(4, "Manquehue")
    private val lasTorres = Location(5, "Las Torres")
    private val principeDeGales = Location(6, "Principe de Gales")
    private val losDominicos = Location(7, "Los Dominicos")
    private val pedroDeValdivia = Location(8, "Pedro de Valdivia")
    private val nunoa = Location(9, "Ñuñoa")
    private val losHeroes = Location(10, "Los Heroes")

    private val demoTickets = listOf(
        DemoTicket(
            title = "Printer not working",
            category = "Other",
            priority = Urgency.Low,
            branch = estacionCentral,
            description = "Yesterday the printer started to dye the whole sheet yellow",
            assignedTo = 4,
            createdAt = at(17, 12, 10),
            lastUpdate = at(20, 15, 35),
            comments = listOf(4L to "Fixed, try printing a new page."),
            status = TicketStatus.Closed
        ),
        DemoTicket(
            title = "Slow Computer",
            category = "Software",
            priority = Urgency.High,
            branch = tobalaba,
            description = "The computer has too much input lag",
            assignedTo = 5,
            createdAt = at(17, 11, 10),
            lastUpdate = at(20, 8, 35),
            comments = listOf(4L to "Fixed."),
            status = TicketStatus.Closed
        ),
        DemoTicket(
            title = "Register keyboard not responding",
            category = "Hardware",
            priority = Urgency.Low,
            branch = plazaEgana,
            description = "The keyboard at register 2 stopped working after a liquid spill this morning.",
            assignedTo = 5,
            createdAt = at(14, 9, 15),
            lastUpdate = at(14, 10, 30),
            comments = listOf(
                5L to "I went to check the register, the keyboard was replaced with a new one from inventory."
            ),
            status = TicketStatus.Closed
        ),
        DemoTicket(
            title = "No internal network connection",
            category = "Network",
            priority = Urgency.High,
            branch = losDominicos,
            description = "The entire branch lost network connection. The main router is blinking red and there is no access to the sales system.",
            assignedTo = 6,
            createdAt = at(15, 14, 20),
            lastUpdate = at(15, 14, 45),
            comments = listOf(
                6L to "Attempting to remotely reboot the network equipment.",
                6L to "Reboot failed, coordinating an emergency technical visit with the internet provider."
            ),
            status = TicketStatus.OnHold
        ),
        DemoTicket(
            title = "Portal credentials error",
            category = "Access",
            priority = Urgency.Medium,
            branch = nunoa,
            description = "The new supervisor cannot access the management portal; the system indicates that her user is blocked.",
            assignedTo = 4,
            createdAt = at(16, 10, 5),
            lastUpdate = at(16, 11, 10),
            comments = listOf(
                4L to "Password reset and account unlocked. She was asked to change the password on her next login."
            ),
            status = TicketStatus.Closed
        ),
        DemoTicket(
            title = "Blue screen on warehouse computer",
            category = "Software",
            priority = Urgency.High,
            branch = tobalaba,
            description = "The computer restarts on its own after 5 minutes of use and shows a blue screen with a memory error code.",
            assignedTo = 8,
            createdAt = at(17, 11, 45),
            lastUpdate = at(18, 9, 20),
            comments = listOf(
                8L to "On my way to the branch to diagnose the equipment.",
                8L to "Identified a defective RAM module, requesting a replacement from central.",
                8L to "Replacement installed and equipment tested. Working correctly."
            ),
            status = TicketStatus.Closed
        ),
        DemoTicket(
            title = "Hallway camera disconnected",
            category = "Security",
            priority = Urgency.Medium,
            branch = principeDeGales,
            description = "The main hallway IP camera shows a black screen on the security room monitor.",
            assignedTo = 7,
            createdAt = at(18, 8, 30),
            lastUpdate = at(18, 15, 0),
            comments = listOf(
                7L to "The network cable on the switch was loose, I plugged it back in and it now shows an image."
            ),
            status = TicketStatus.Closed
        ),
        DemoTicket(
            title = "System update failed",
            category = "Software",
            priority = Urgency.Low,
            branch = lasTorres,
            description = "The management team's computer constantly asks to reboot but the update fails at 90%.",
            assignedTo = 5,
            createdAt = at(19, 16, 10),
            lastUpdate = at(19, 16, 15),
            comments = listOf(
                5L to "Deploying a fix patch from the server. Let me know if the boot loop persists tomorrow."
            ),
            status = TicketStatus.InProgress
        ),
        DemoTicket(
            title = "Barcode scanner no laser",
            category = "Hardware",
            priority = Urgency.Medium,
            branch = manquehue,
            description = "The main register's scanner does not emit light to read products, although it makes the startup sound.",
            assignedTo = 4,
            createdAt = at(19, 18, 40),
            lastUpdate = at(20, 10, 0),
            comments = listOf(
                4L to "Please try connecting it to another USB port on the terminal.",
                4L to "Still not working, I will prepare a new one to take on the afternoon visit."
            ),
            status = TicketStatus.New
        ),
        DemoTicket(
            title = "Multiple firewall alerts",
            category = "Security",
            priority = Urgency.Critical,
            branch = pedroDeValdivia,
            description = "The system reports multiple failed login attempts from an external IP to the local database server.",
            assignedTo = 8,
            createdAt = at(20, 9, 15),
            lastUpdate = at(20, 11, 30),
            comments = listOf(
                8L to "Reviewing logs and blocking the attacking IP range on the branch firewall.",
                8L to "IP successfully blocked. Administrator passwords were rotated as a precaution."
            ),
            status = TicketStatus.Closed
        ),
        DemoTicket(
            title = "Monitor constantly flickering",
            category = "Hardware",
            priority = Urgency.Low,
            branch = estacionCentral,
            description = "The customer inquiry terminal screen flickers and shows horizontal lines every few minutes.",
            assignedTo = 6,
            createdAt = at(20, 12, 20),
            lastUpdate = at(20, 12, 20),
            comments = listOf(
                6L to "Ticket received. I will check the HDMI or DisplayPort cable connections when another technician is freed up."
            ),
            status = TicketStatus.Queued
        ),
        DemoTicket(
            title = "Access request for intern",
            category = "Access",
            priority = Urgency.Low,
            branch = losHeroes,
            description = "We need to create internal system and email access for the new intern starting today.",
            assignedTo = 7,
            createdAt = at(20, 14, 0),
            lastUpdate = at(20, 14, 50),
            comments = listOf(
                7L to "Account successfully created. Temporary credentials were sent to the supervising manager's email."
            ),
            status = TicketStatus.Closed
        )
    )

    private val demoTeams = listOf(
        "Team 1" to listOf(4L, 5L, 7L),
        "Team 2" to listOf(6L, 4L, 8L),
        "Team 3" to listOf(7L, 8L, 5L)
    )

    private val creatorIds = listOf(1L, 2L, 3L)

    @Synchronized
    fun load(): Pair<Int, Int>? {
        if (loaded) return null
        loaded = true
        Persistence.saveDemoLoaded(true)

        demoTickets.forEachIndexed { index, demo ->
            val span = Duration.between(demo.createdAt, demo.lastUpdate)
            val count = demo.comments.size
            val comments = demo.comments.mapIndexed { i, (author, text) ->
                Triple(author, text, demo.createdAt.plus(span.multipliedBy((i + 1).toLong()).dividedBy(count.toLong())))
            }
            TicketRepository.importTicket(
                idUser = creatorIds[index % creatorIds.size],
                title = demo.title,
                category = demo.category,
                priority = demo.priority,
                branch = demo.branch,
                description = demo.description,
                status = demo.status,
                createdAt = demo.createdAt,
                lastUpdate = demo.lastUpdate,
                peopleInCharge = demo.assignedTo,
                comments = comments
            )
        }

        val teamsCreated = demoTeams.count { (name, members) ->
            TeamRepository.importTeam(name, members) != null
        }
        return demoTickets.size to teamsCreated
    }
}
