package com.example.panalsuite.ui.teams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Team
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.TeamRepository
import com.example.panalsuite.repository.UserRepository
import com.example.panalsuite.ui.home.EmptyStateText

@Composable
fun TeamsScreen(user: User) {
    val context = LocalContext.current
    val operators = remember { UserRepository(context).getActiveOperators() }
    val operatorsById = remember(operators) { operators.associateBy { it.idUser } }
    // Leer del repositorio aqui hace que la lista se redibuje al crear, editar o borrar
    val teams = TeamRepository.getTeams()
    val canManage = Permissions.ManageTeams in user.role.permits

    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Team?>(null) }
    var deleting by remember { mutableStateOf<Team?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.teams_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (canManage) {
                Button(onClick = { creating = true }, shape = RoundedCornerShape(16.dp)) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = stringResource(R.string.teams_new))
                }
            }
        }

        if (teams.isEmpty()) {
            EmptyStateText(text = stringResource(R.string.teams_empty))
        } else {
            teams.forEach { team ->
                TeamCard(
                    team = team,
                    operatorsById = operatorsById,
                    canManage = canManage,
                    onEdit = { editing = team },
                    onDelete = { deleting = team }
                )
            }
        }
    }

    if (creating) {
        TeamEditorDialog(
            titleRes = R.string.teams_create_title,
            initialName = "",
            initialMembers = emptySet(),
            operators = operators,
            nameTaken = { name -> TeamRepository.getTeams().any { it.name.equals(name, ignoreCase = true) } },
            onConfirm = { name, members ->
                TeamRepository.createTeam(user, name, members.toList())
                creating = false
            },
            onDismiss = { creating = false }
        )
    }

    editing?.let { team ->
        TeamEditorDialog(
            titleRes = R.string.teams_edit_title,
            initialName = team.name,
            initialMembers = team.memberIds.toSet(),
            operators = operators,
            nameTaken = { name ->
                TeamRepository.getTeams().any {
                    it.idTeam != team.idTeam && it.name.equals(name, ignoreCase = true)
                }
            },
            onConfirm = { name, members ->
                TeamRepository.updateTeam(user, team.idTeam, name, members.toList())
                editing = null
            },
            onDismiss = { editing = null }
        )
    }

    deleting?.let { team ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(text = stringResource(R.string.teams_delete_title)) },
            text = { Text(text = stringResource(R.string.teams_delete_message, team.name)) },
            confirmButton = {
                TextButton(onClick = {
                    TeamRepository.deleteTeam(user, team.idTeam)
                    deleting = null
                }) {
                    Text(text = stringResource(R.string.teams_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text(text = stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun TeamCard(
    team: Team,
    operatorsById: Map<Long, User>,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val memberNames = team.memberIds
        .mapNotNull { operatorsById[it] }
        .joinToString(separator = ", ") { "${it.name} ${it.lastName}" }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.teams_member_count, team.memberIds.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = if (memberNames.isEmpty()) stringResource(R.string.teams_members_empty) else memberNames,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canManage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Text(text = stringResource(R.string.teams_edit))
                    }
                    TextButton(onClick = onDelete) {
                        Text(text = stringResource(R.string.teams_delete))
                    }
                }
            }
        }
    }
}

// Formulario compartido para crear y editar: nombre + seleccion de Operators.
@Composable
private fun TeamEditorDialog(
    titleRes: Int,
    initialName: String,
    initialMembers: Set<Long>,
    operators: List<User>,
    nameTaken: (String) -> Boolean,
    onConfirm: (String, Set<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var members by remember { mutableStateOf(initialMembers) }

    fun toggle(idUser: Long) {
        members = if (idUser in members) members - idUser else members + idUser
    }

    val trimmed = name.trim()
    val taken = trimmed.isNotEmpty() && nameTaken(trimmed)
    val canSave = trimmed.isNotEmpty() && !taken

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(titleRes), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.teams_field_name)) },
                    isError = taken,
                    supportingText = {
                        if (taken) Text(text = stringResource(R.string.teams_name_taken))
                    }
                )
                Text(
                    text = stringResource(R.string.teams_field_members),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (operators.isEmpty()) {
                    Text(
                        text = stringResource(R.string.teams_no_operators),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                operators.forEach { operator ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { toggle(operator.idUser) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = operator.idUser in members,
                            onCheckedChange = { toggle(operator.idUser) }
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${operator.name} ${operator.lastName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(trimmed, members) },
                enabled = canSave
            ) {
                Text(text = stringResource(R.string.teams_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        }
    )
}
