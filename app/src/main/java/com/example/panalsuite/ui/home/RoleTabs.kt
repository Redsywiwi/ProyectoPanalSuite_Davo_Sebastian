package com.example.panalsuite.ui.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.panalsuite.R
import com.example.panalsuite.model.Role

// Que contenido muestra cada tab
enum class TabType { HOME, TICKETS, OTHER, PROFILE }

data class BottomTab(
    @StringRes
    val label: Int,
    val icon: ImageVector,
    val type: TabType
)

private val HomeTab = BottomTab(R.string.tab_home, Icons.Rounded.Home, TabType.HOME)
private val ProfileTab = BottomTab(R.string.tab_profile, Icons.Rounded.Person, TabType.PROFILE)

// Pestañas de la barra inferior según el rol
fun Role.bottomTabs(): List<BottomTab> = when (this) {
    Role.CommonUser -> listOf(
        HomeTab,
        BottomTab(R.string.tab_my_tickets, Icons.Rounded.ConfirmationNumber, TabType.TICKETS),
        ProfileTab
    )
    Role.Operator -> listOf(
        HomeTab,
        BottomTab(R.string.tab_assigned, Icons.Rounded.AssignmentInd, TabType.TICKETS),
        ProfileTab
    )
    Role.Supervisor -> listOf(
        HomeTab,
        BottomTab(R.string.tab_team, Icons.Rounded.Groups, TabType.OTHER),
        ProfileTab
    )
    Role.Management -> listOf(
        HomeTab,
        BottomTab(R.string.tab_summary, Icons.Rounded.BarChart, TabType.OTHER),
        ProfileTab
    )
    Role.Administrator -> listOf(
        HomeTab,
        BottomTab(R.string.tab_manage, Icons.Rounded.AdminPanelSettings, TabType.OTHER),
        ProfileTab
    )
}

@StringRes
fun Role.bannerTitle(): Int = when (this) {
    Role.CommonUser -> R.string.banner_title_common
    Role.Operator -> R.string.banner_title_operator
    Role.Supervisor -> R.string.banner_title_supervisor
    Role.Management -> R.string.banner_title_management
    Role.Administrator -> R.string.banner_title_admin
}

@StringRes
fun Role.bannerDescription(): Int = when (this) {
    Role.CommonUser -> R.string.banner_desc_common
    Role.Operator -> R.string.banner_desc_operator
    Role.Supervisor -> R.string.banner_desc_supervisor
    Role.Management -> R.string.banner_desc_management
    Role.Administrator -> R.string.banner_desc_admin
}
