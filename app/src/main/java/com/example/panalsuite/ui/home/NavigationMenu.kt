package com.example.panalsuite.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.User

data class DrawerItem(
    @StringRes
    val label: Int,
    val icon: ImageVector,
    val route: String
)

/**
Gemelo para agregar una opcion copie una línea, cambia el texto en
(strings.xml), el ícono y la ruta.
El menu desplegable tipico se llama drawer, cajon xd
pa q sepa
 */


val drawerItems = listOf(
    DrawerItem(R.string.drawer_settings, Icons.Rounded.Settings, "settings"),
    DrawerItem(R.string.drawer_notifications, Icons.Rounded.Notifications, "notifications"),
    DrawerItem(R.string.drawer_history, Icons.Rounded.History, "history"),
    DrawerItem(R.string.drawer_support, Icons.Rounded.SupportAgent, "support"),
    DrawerItem(R.string.drawer_about, Icons.Rounded.Info, "about"),
)

@Composable
fun HomeDrawerContent(
    user: User,
    onItemClick: (DrawerItem) -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "${user.name} ${user.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = user.role.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Opciones
        Column(modifier = Modifier.padding(top = 12.dp)) {
            drawerItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(text = stringResource(item.label)) },
                    icon = { Icon(imageVector = item.icon, contentDescription = null) },
                    selected = false,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.home_logout)) },
                icon = { Icon(imageVector = Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                selected = false,
                onClick = onLogoutClick,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}