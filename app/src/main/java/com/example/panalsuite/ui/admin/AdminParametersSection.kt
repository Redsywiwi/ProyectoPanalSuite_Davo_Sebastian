package com.example.panalsuite.ui.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.ParamError
import com.example.panalsuite.repository.ParametersRepository


@Composable
internal fun ParametersSection(actor: User) {
    val categories = ParametersRepository.getCategories()
    val locations = ParametersRepository.getLocations()

    ParameterCard(
        titleRes = R.string.admin_param_categories,
        newTitleRes = R.string.admin_param_new_category,
        editTitleRes = R.string.admin_param_edit_category,
        items = categories.map { it to it },
        onAdd = { name -> ParametersRepository.addCategory(actor, name) },
        onRename = { key, name -> ParametersRepository.renameCategory(actor, key, name) },
        onDelete = { key -> ParametersRepository.deleteCategory(actor, key) }
    )

    ParameterCard(
        titleRes = R.string.admin_param_locations,
        newTitleRes = R.string.admin_param_new_location,
        editTitleRes = R.string.admin_param_edit_location,
        items = locations.map { it.idLocation.toString() to it.name },
        onAdd = { name -> ParametersRepository.addLocation(actor, name) },
        onRename = { key, name -> ParametersRepository.renameLocation(actor, key.toLong(), name) },
        onDelete = { key -> ParametersRepository.deleteLocation(actor, key.toLong()) }
    )
}

@Composable
private fun ParameterCard(
    @StringRes titleRes: Int,
    @StringRes newTitleRes: Int,
    @StringRes editTitleRes: Int,
    items: List<Pair<String, String>>,
    onAdd: (String) -> ParamError?,
    onRename: (key: String, name: String) -> ParamError?,
    onDelete: (key: String) -> ParamError?
) {
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Pair<String, String>?>(null) }
    var deleting by remember { mutableStateOf<Pair<String, String>?>(null) }
    var deleteError by remember { mutableStateOf<ParamError?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { deleteError = null; adding = true }) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = stringResource(R.string.admin_param_add))
                }
            }

            deleteError?.let {
                Text(
                    text = stringResource(it.messageRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            items.forEach { item ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.second,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { deleteError = null; editing = item }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.admin_edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { deleteError = null; deleting = item }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.admin_delete)
                        )
                    }
                }
            }
        }
    }

    if (adding) {
        NameDialog(
            titleRes = newTitleRes,
            initial = "",
            onConfirm = { name -> onAdd(name) },
            onDismiss = { adding = false }
        )
    }

    editing?.let { item ->
        NameDialog(
            titleRes = editTitleRes,
            initial = item.second,
            onConfirm = { name -> onRename(item.first, name) },
            onDismiss = { editing = null }
        )
    }

    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(text = stringResource(R.string.admin_param_delete_title), fontWeight = FontWeight.Bold) },
            text = { Text(text = stringResource(R.string.admin_param_delete_message, item.second)) },
            confirmButton = {
                TextButton(onClick = {
                    deleteError = onDelete(item.first)
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
private fun NameDialog(
    @StringRes titleRes: Int,
    initial: String,
    onConfirm: (String) -> ParamError?,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf<ParamError?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(titleRes), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_field_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
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
                val result = onConfirm(name)
                if (result == null) onDismiss() else error = result
            }) {
                Text(text = stringResource(R.string.admin_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        }
    )
}
