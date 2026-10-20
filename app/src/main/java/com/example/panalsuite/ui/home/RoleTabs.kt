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
enum class TabType { HOME, TICKETS, TEAM, SUMMARY, ADMIN, OTHER, PROFILE }

data class BottomTab(
    @StringRes
    val label: Int,
    val icon: ImageVector,
    val type: TabType
)

private val HomeTab = BottomTab(R.string.tab_home, Icons.Rounded.Home, TabType.HOME)
private val ProfileTab = BottomTab(R.string.tab_profile, Icons.Rounded.Person, TabType.PROFILE)

fun Role.bottomTabs(): List<BottomTab> = when (this) {
    Role.CommonUser -> listOf(
        HomeTab,
        BottomTab(R.string.tab_my_tickets, Icons.Rounded.ConfirmationNumber, TabType.TICKETS),
        ProfileTab
    )
    Role.Operator -> listOf(
        BottomTab(R.string.tab_assigned, Icons.Rounded.AssignmentInd, TabType.TICKETS),
        ProfileTab
    )
    Role.Supervisor -> listOf(
        HomeTab,
        BottomTab(R.string.tab_team, Icons.Rounded.Groups, TabType.TEAM),
        ProfileTab
    )
    Role.Management -> listOf(
        HomeTab,
        BottomTab(R.string.tab_summary, Icons.Rounded.BarChart, TabType.SUMMARY),
        ProfileTab
    )
    Role.Administrator -> listOf(
        BottomTab(R.string.tab_manage, Icons.Rounded.AdminPanelSettings, TabType.ADMIN),
        ProfileTab
    )
}