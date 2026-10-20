package com.example.panalsuite.ui.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.ParamError
import com.example.panalsuite.repository.UserError

@Composable
fun AdminScreen(user: User) {
    val sections = listOf(
        R.string.admin_section_users to Permissions.ManageUsers,
        R.string.admin_section_permissions to Permissions.ManagePermissions,
        R.string.admin_section_parameters to Permissions.ManageParameters
    ).filter { it.second in user.role.permits }
    if (sections.isEmpty()) return

    var selected by rememberSaveable { mutableIntStateOf(0) }
    val current = selected.coerceIn(0, sections.lastIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.admin_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.admin_memory_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEachIndexed { index, section ->
                FilterChip(
                    selected = index == current,
                    onClick = { selected = index },
                    label = { Text(text = stringResource(section.first)) }
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (sections[current].second) {
                Permissions.ManageUsers -> UsersSection(actor = user)
                Permissions.ManagePermissions -> PermissionsSection(actor = user)
                else -> ParametersSection(actor = user)
            }
        }
    }
}

@StringRes
internal fun UserError.messageRes(): Int = when (this) {
    UserError.NotAllowed -> R.string.admin_err_not_allowed
    UserError.MissingFields -> R.string.admin_err_missing
    UserError.EmailTaken -> R.string.admin_err_email_taken
    UserError.NotFound -> R.string.admin_err_not_found
    UserError.CannotModifySelf -> R.string.admin_err_self
    UserError.LastAdministrator -> R.string.admin_err_last_admin
}

@StringRes
internal fun ParamError.messageRes(): Int = when (this) {
    ParamError.NotAllowed -> R.string.admin_err_not_allowed
    ParamError.Empty -> R.string.admin_err_param_empty
    ParamError.Duplicate -> R.string.admin_err_param_duplicate
    ParamError.NotFound -> R.string.admin_err_not_found
    ParamError.LastOne -> R.string.admin_err_last_one
}
