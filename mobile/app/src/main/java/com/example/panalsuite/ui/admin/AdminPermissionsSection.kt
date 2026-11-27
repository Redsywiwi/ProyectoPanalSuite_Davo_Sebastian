package com.example.panalsuite.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.RolePermissions
import com.example.panalsuite.model.User

@Composable
internal fun PermissionsSection(actor: User) {
    var selectedRole by rememberSaveable { mutableStateOf(Role.CommonUser) }

    val permits = selectedRole.permits
    val customized = RolePermissions.isCustomized(selectedRole)

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Role.entries.forEach { role ->
            FilterChip(
                selected = role == selectedRole,
                onClick = { selectedRole = role },
                label = { Text(text = role.name) }
            )
        }
    }

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
                    text = stringResource(R.string.admin_perm_title, selectedRole.name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        if (customized) R.string.admin_perm_customized else R.string.admin_perm_default
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (customized) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Permissions.entries.forEach { permission ->
                val locked = RolePermissions.isLocked(selectedRole, permission)
                val toggle = {
                    val updated = if (permission in permits) permits - permission else permits + permission
                    RolePermissions.update(actor, selectedRole, updated)
                    Unit
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !locked) { toggle() }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = permission.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (locked) {
                            Text(
                                text = stringResource(R.string.admin_perm_locked),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = permission in permits,
                        onCheckedChange = { toggle() },
                        enabled = !locked
                    )
                }
            }

            if (customized) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { RolePermissions.reset(actor, selectedRole) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(R.string.admin_perm_reset))
                }
            }
        }
    }
}
