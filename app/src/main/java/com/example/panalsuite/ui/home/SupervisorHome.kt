package com.example.panalsuite.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.TicketFilters
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.TicketRepository
import com.example.panalsuite.ui.components.TicketCard

@Composable
fun SupervisorHomeContent(
    user: User,
    onMenuClick: () -> Unit,
    onTicketClick: (Long) -> Unit,
    filters: TicketFilters,
    onFiltersChange: (TicketFilters) -> Unit
) {
    val searching = filters.isActive
    val shown = if (searching) {
        TicketRepository.searchAllTickets(user, filters)
    } else {
        TicketRepository.getAllTickets(user)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HomeHeader(name = user.name, role = user.role.name, onMenuClick = onMenuClick)
        TicketSearchBar(filters = filters, onFiltersChange = onFiltersChange)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(
                title = stringResource(
                    if (searching) R.string.home_search_results else R.string.home_all_tickets
                )
            )
            if (shown.isEmpty()) {
                EmptyStateText(
                    text = stringResource(
                        if (searching) R.string.tickets_no_results else R.string.tickets_empty_all
                    )
                )
            } else {
                shown.forEach { ticket ->
                    TicketCard(
                        ticket = ticket,
                        onClick = { onTicketClick(ticket.idTicket) }
                    )
                }
            }
        }
    }
}