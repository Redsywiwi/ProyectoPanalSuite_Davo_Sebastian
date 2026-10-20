package com.example.panalsuite.ui.tickets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Ticket
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.Session
import com.example.panalsuite.repository.TicketRepository
import com.example.panalsuite.repository.UserRepository
import com.example.panalsuite.ui.components.AssignOperatorDialog
import com.example.panalsuite.ui.components.StatusChip
import com.example.panalsuite.ui.components.toDisplayString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(
    ticketId: Long,
    onBackClick: () -> Unit
) {
    val user = Session.currentUser ?: return
    val ticket = TicketRepository.getTicketForViewer(ticketId, user)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ticket_details_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (ticket != null && ticket.idUser == user.idUser) {
                        IconButton(
                            onClick = { TicketRepository.setPinned(ticket.idTicket, !ticket.pinned) }
                        ) {
                            Icon(
                                imageVector = if (ticket.pinned) Icons.Rounded.PushPin
                                else Icons.Outlined.PushPin,
                                contentDescription = stringResource(
                                    if (ticket.pinned) R.string.ticket_unpin else R.string.ticket_pin
                                ),
                                tint = if (ticket.pinned) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (ticket == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.ticket_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            TicketDetailsContent(
                ticket = ticket,
                user = user,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun TicketDetailsContent(ticket: Ticket, user: User, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val usersById = remember { UserRepository(context).getUsers().associateBy { it.idUser } }
    val userLabel: (Long) -> String = { id ->
        usersById[id]?.let { "${it.name} ${it.lastName}" }
            ?: context.getString(R.string.ticket_assigned_format, id)
    }

    val canChangeStatus = Permissions.ChangeStatus in user.role.permits
    val canComment = Permissions.Comment in user.role.permits
    val canReassign = Permissions.Reassign in user.role.permits
    val operators = remember { UserRepository(context).getActiveOperators() }
    var showAssignDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ticket_number_format, ticket.idTicket),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            StatusChip(text = ticket.status.StatusName)
        }

        Text(
            text = ticket.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        SectionCard {
            DetailRow(stringResource(R.string.new_ticket_field_category), ticket.category)
            DetailDivider()
            DetailRow(stringResource(R.string.new_ticket_field_priority), ticket.priority.priorityName)
            DetailDivider()
            DetailRow(stringResource(R.string.new_ticket_field_location), ticket.branch.name)
            DetailDivider()
            DetailRow(
                stringResource(R.string.ticket_assigned_label),
                ticket.peopleInCharge?.let(userLabel) ?: stringResource(R.string.ticket_unassigned)
            )
            DetailDivider()
            DetailRow(stringResource(R.string.ticket_created_label), ticket.createdAt.toDisplayString())
            DetailDivider()
            DetailRow(stringResource(R.string.ticket_last_update_label), ticket.lastUpdate.toDisplayString())
        }

        SectionCard {
            Text(
                text = stringResource(R.string.new_ticket_field_description),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (canChangeStatus) {
            SectionCard {
                Text(
                    text = stringResource(R.string.ticket_change_status),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusDropdown(
                    selected = ticket.status,
                    onSelected = { TicketRepository.changeStatus(ticket.idTicket, it, user) }
                )
            }
        }

        if (canReassign) {
            SectionCard {
                Text(
                    text = stringResource(R.string.ticket_assign_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(onClick = { showAssignDialog = true }) {
                    Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.ticket_assign_action))
                }
            }
        }

        // Todos los que ven el ticket los leen, solo con permiso Comment se escribe
        SectionCard {
            Text(
                text = stringResource(R.string.ticket_comments_title, ticket.commentary.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (ticket.commentary.isEmpty()) {
                Text(
                    text = stringResource(R.string.ticket_comments_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ticket.commentary.forEachIndexed { index, comment ->
                    if (index > 0) DetailDivider()
                    CommentItem(
                        author = userLabel(comment.idUser),
                        date = comment.commentaryDate.toDisplayString(),
                        text = comment.text
                    )
                }
            }
            if (canComment) {
                DetailDivider()
                CommentInput(
                    onSend = { TicketRepository.addComment(ticket.idTicket, user, it) }
                )
            }
        }

        if (ticket.developmentHistory.isNotEmpty()) {
            SectionCard {
                Text(
                    text = stringResource(R.string.ticket_history_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ticket.developmentHistory.asReversed().forEachIndexed { index, entry ->
                    if (index > 0) DetailDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${entry.modifiedField}: ${entry.previousValue} → ${entry.newValue}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${userLabel(entry.idUser)} · ${entry.date.toDisplayString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAssignDialog) {
        AssignOperatorDialog(
            currentAssigneeId = ticket.peopleInCharge,
            operators = operators,
            onAssign = { operator ->
                TicketRepository.assignTicket(
                    idTicket = ticket.idTicket,
                    target = operator,
                    actor = user,
                    previousAssigneeName = ticket.peopleInCharge?.let(userLabel)
                )
                showAssignDialog = false
            },
            onDismiss = { showAssignDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selected: TicketStatus,
    onSelected: (TicketStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.StatusName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.ticket_status_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TicketStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.StatusName) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CommentItem(author: String, date: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = author,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CommentInput(onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.ticket_comment_hint)) },
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Button(
            onClick = {
                onSend(text)
                text = ""
            },
            enabled = text.isNotBlank()
        ) {
            Text(stringResource(R.string.ticket_comment_send))
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}