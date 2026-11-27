package com.example.panalsuite.model

import java.time.LocalDate

data class TicketFilters(
    val query: String = "",
    val statuses: Set<TicketStatus> = emptySet(),
    val priorities: Set<Urgency> = emptySet(),
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null
) {
    val hasAdvancedFilters: Boolean
        get() = statuses.isNotEmpty() || priorities.isNotEmpty() || fromDate != null || toDate != null

    val isActive: Boolean
        get() = query.isNotBlank() || hasAdvancedFilters
}