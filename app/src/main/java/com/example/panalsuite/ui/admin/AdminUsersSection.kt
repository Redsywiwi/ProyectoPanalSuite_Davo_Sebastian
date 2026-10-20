package com.example.panalsuite.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.UserError
import com.example.panalsuite.repository.UserRepository
import com.example.panalsuite.ui.components.StatusChip
import com.example.panalsuite.ui.home.EmptyStateText

// CRUD de usuarios
@Composable
internal fun UsersSection(actor: User) {
    val context = LocalContext.current
    val repository = remember { UserRepository(context) }
    val users = repository.getUsers().sortedBy { it.idUser }

    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<User?>(null) }
    var deleting by remember { mutableStateOf<User?>(null) }
    var deleteError by remember { mutableStateOf<UserError?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.admin_users_count, users.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Button(onClick = { creating = true }, shape = RoundedCornerShape(16.dp)) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = stringResource(R.string.admin_users_new))
        }
    }

    deleteError?.let {
        Text(
            text = stringResource(it.messageRes()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }

    if (users.isEmpty()) {
        EmptyStateText(text = stringResource(R.string.admin_users_empty))
    } else {
        users.forEach { target ->
            UserCard(
                target = target,
                isSelf = target.idUser == actor.idUser,
                onEdit = { deleteError = null; editing = target },
                onDelete = { deleteError = null; deleting = target }
            )
        }
    }

    if (creating) {
        UserEditorDialog(
            initial = null,
            nextId = repository.nextUserId(),
            isSelf = false,
            onConfirm = { name, lastName, email, password, role, _ ->
                repository.createUser(actor, name, lastName, email, password, role)
            },
            onDismiss = { creating = false }
        )
    }

    editing?.let { target ->
        UserEditorDialog(
            initial = target,
            nextId = target.idUser,
            isSelf = target.idUser == actor.idUser,
            onConfirm = { name, lastName, email, password, role, active ->
                repository.updateUser(actor, target.idUser, name, lastName, email, password, role, active)
            },
            onDismiss = { editing = null }
        )
    }

    deleting?.let { target ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(text = stringResource(R.string.admin_delete_user_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = stringResource(
                        R.string.admin_delete_user_message,
                        "${target.name} ${target.lastName}"
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteError = repository.deleteUser(actor, target.idUser)
                    deleting = null
                }) {
                    Text(text = stringResource(R.string.admin_delete), color = MaterialTheme.colorScheme.error)
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
private fun UserCard(
    target: User,
    isSelf: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${target.name} ${target.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.admin_user_id_email, target.idUser, target.email),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(text = target.role.name)
                    if (!target.active) {
                        Text(
                            text = stringResource(R.string.admin_user_inactive),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.admin_edit),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete, enabled = !isSelf) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.admin_delete)
                )
            }
        }
    }
}
@Composable
private fun UserEditorDialog(
    initial: User?,
    nextId: Long,
    isSelf: Boolean,
    onConfirm: (
        name: String,
        lastName: String,
        email: String,
        password: String,
        role: Role,
        active: Boolean
    ) -> UserError?,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var lastName by remember { mutableStateOf(initial?.lastName ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var password by remember { mutableStateOf(initial?.password ?: "") }
    var role by remember { mutableStateOf(initial?.role ?: Role.CommonUser) }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var error by remember { mutableStateOf<UserError?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (initial == null) R.string.admin_user_create_title else R.string.admin_user_edit_title
                ),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(
                        if (initial == null) R.string.admin_user_next_id else R.string.admin_user_id,
                        nextId
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_field_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_field_lastname)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_field_password)) },
                    singleLine = true
                )
                RoleDropdown(role = role, enabled = !isSelf, onSelected = { role = it })
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_field_email)) },
                    supportingText = {
                        if (initial == null) Text(stringResource(R.string.admin_field_email_hint))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                if (initial != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.admin_field_active),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(checked = active, onCheckedChange = { active = it }, enabled = !isSelf)
                    }
                }
                if (isSelf) {
                    Text(
                        text = stringResource(R.string.admin_self_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                error?.let {
                    Text(
                        text = stringResource(it.messageRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val result = onConfirm(name, lastName, email, password, role, active)
                if (result == null) onDismiss() else error = result
            }) {
                Text(
                    text = stringResource(
                        if (initial == null) R.string.admin_create else R.string.admin_save
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleDropdown(role: Role, enabled: Boolean, onSelected: (Role) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = role.name,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.admin_field_role)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Role.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
