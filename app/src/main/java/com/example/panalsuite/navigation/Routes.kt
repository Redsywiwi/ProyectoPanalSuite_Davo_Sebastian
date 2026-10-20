package com.example.panalsuite.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val NEW_TICKET = "new_ticket"

    const val TICKET_ID_ARG = "ticketId"
    const val TICKET_DETAILS = "ticket_details/{$TICKET_ID_ARG}"

    fun ticketDetails(ticketId: Long) = "ticket_details/$ticketId"
}
