package com.example.panalsuite.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Ticket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
fun LocalDateTime.toDisplayString(): String = format(dateFormatter)

@Composable
fun TicketCard(
    ticket: Ticket,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    TicketCard(
        modifier = modifier,
        ticketNumber = stringResource(R.string.ticket_number_format, ticket.idTicket),
        status = ticket.status.StatusName,
        title = ticket.title,
        description = ticket.description,
        priority = ticket.priority.priorityName,
        assignedName = ticket.peopleInCharge?.let {
            stringResource(R.string.ticket_assigned_format, it)
        } ?: stringResource(R.string.ticket_unassigned),
        lastUpdate = stringResource(
            R.string.ticket_updated_format,
            ticket.lastUpdate.toDisplayString()
        ),
        onClick = onClick
    )
}

@Composable
fun TicketCard(
    modifier: Modifier = Modifier,
    ticketNumber: String = stringResource(R.string.ticket_number_placeholder),
    status: String = stringResource(R.string.ticket_status_placeholder),
    title: String = stringResource(R.string.ticket_title_placeholder),
    description: String = stringResource(R.string.ticket_description_placeholder),
    priority: String = stringResource(R.string.ticket_priority_placeholder),
    assignedName: String = stringResource(R.string.ticket_assigned_placeholder),
    lastUpdate: String = stringResource(R.string.ticket_updated_placeholder),
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(20.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    val content: @Composable ColumnScope.() -> Unit = {
        TicketCardContent(
            ticketNumber = ticketNumber,
            status = status,
            title = title,
            description = description,
            priority = priority,
            assignedName = assignedName,
            lastUpdate = lastUpdate
        )
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
private fun TicketCardContent(
    ticketNumber: String,
    status: String,
    title: String,
    description: String,
    priority: String,
    assignedName: String,
    lastUpdate: String
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Ticket number + status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ticketNumber,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            StatusChip(text = status)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Priority / Assigned / Last update
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterText(text = priority, modifier = Modifier.weight(1f))
            FooterText(text = assignedName, modifier = Modifier.weight(1.4f))
            FooterText(text = lastUpdate, modifier = Modifier.weight(1.4f))
        }
    }
}

@Composable
fun StatusChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun FooterText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
