package com.example.panalsuite.model

enum class TicketStatus(
    val StatusName: String
) {

    New("Pending"),
    Queued("In Queue"),
    InProgress("In Progress"),
    OnHold("On Hold"),
    Closed("Closed")
}