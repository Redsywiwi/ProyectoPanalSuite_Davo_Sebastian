package com.example.panalsuite.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.Ticket
import com.example.panalsuite.model.TicketFilters
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.Session
import com.example.panalsuite.repository.TicketRepository
import com.example.panalsuite.ui.components.TicketCard
import com.example.panalsuite.ui.management.ManagementDashboardScreen
import com.example.panalsuite.ui.management.ManagementSummaryScreen
import com.example.panalsuite.ui.teams.TeamsScreen
import com.example.panalsuite.ui.theme.PanalSuiteTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit,
    onDrawerNavigate: (route: String) -> Unit = {},
    onNewTicketClick: () -> Unit = {},
    onTicketClick: (ticketId: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user = Session.currentUser ?: return
    val tabs = remember(user.role) { user.role.bottomTabs() }
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var filters by rememberSaveable(stateSaver = TicketFiltersSaver) { mutableStateOf(TicketFilters()) }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                user = user,
                onItemClick = { item ->
                    scope.launch { drawerState.close() }
                    onDrawerNavigate(item.route)
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogoutClick()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = { Text(text = stringResource(tab.label)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (tabs[selectedIndex].type) {
                    TabType.HOME -> when (user.role) {
                        Role.Supervisor -> SupervisorHomeContent(
                            user = user,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onTicketClick = onTicketClick,
                            filters = filters,
                            onFiltersChange = { filters = it }
                        )

                        Role.Management -> ManagementDashboardScreen(
                            user = user,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                        else -> HomeContent(
                            user = user,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onNewTicketClick = onNewTicketClick,
                            onTicketClick = onTicketClick,
                            filters = filters,
                            onFiltersChange = { filters = it }
                        )
                    }
                    TabType.TICKETS -> TicketsContent(
                        user = user,
                        filters = filters,
                        onFiltersChange = { filters = it },
                        onTicketClick = onTicketClick
                    )
                    TabType.TEAM -> TeamsScreen(user = user)
                    TabType.SUMMARY -> ManagementSummaryScreen(user = user)
                    TabType.OTHER -> PlaceholderContent(tabs[selectedIndex])
                    TabType.PROFILE -> ProfileContent(user, onLogoutClick)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HOME
// ---------------------------------------------------------------------------

@Composable
private fun HomeContent(
    user: User,
    onMenuClick: () -> Unit,
    onNewTicketClick: () -> Unit,
    onTicketClick: (Long) -> Unit,
    filters: TicketFilters,
    onFiltersChange: (TicketFilters) -> Unit
) {
    val isCommonUser = user.role == Role.CommonUser
    val canSeeAll = Permissions.ViewSystemTray in user.role.permits
    val searching = (isCommonUser || canSeeAll) && filters.isActive

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = if (user.role == Role.CommonUser) 88.dp else 12.dp
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HomeHeader(name = user.name, role = user.role.name, onMenuClick = onMenuClick)
            TicketSearchBar(filters = filters, onFiltersChange = onFiltersChange)
            if (!searching && !canSeeAll) {
                PinnedTicketsCard(userId = user.idUser, onTicketClick = onTicketClick)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(
                    title = stringResource(
                        when {
                            searching -> R.string.home_search_results
                            canSeeAll -> R.string.home_all_tickets
                            else -> R.string.home_recent_tickets
                        }
                    ),
                    action = if (searching || canSeeAll) null else stringResource(R.string.home_see_all)
                )
                if (isCommonUser || canSeeAll) {
                    val shown = when {
                        canSeeAll && searching -> TicketRepository.searchAllTickets(user, filters)
                        canSeeAll -> TicketRepository.getAllTickets(user)
                        searching -> TicketRepository.searchTicketsByUser(user.idUser, filters)
                        else -> TicketRepository.getTicketsByUser(user.idUser).take(3)
                    }
                    if (shown.isEmpty()) {
                        EmptyStateText(
                            text = stringResource(
                                when {
                                    searching -> R.string.tickets_no_results
                                    canSeeAll -> R.string.tickets_empty_all
                                    else -> R.string.tickets_empty
                                }
                            )
                        )
                    } else {
                        shown.forEach { ticket ->
                            TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.idTicket) })
                        }
                    }
                } else {
                    TicketCard()
                }
            }
        }

        if (user.role == Role.CommonUser) {
            NewTicketButton(
                onClick = onNewTicketClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }
    }
}

@Composable
internal fun HomeHeader(name: String, role: String, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_greeting, name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.home_greeting_subtitle, role),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { /* TODO: notificaciones */ }) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PinnedTicketsCard(userId: Long, onTicketClick: (Long) -> Unit) {
    val pinnedTickets = TicketRepository.getPinnedTicketsByUser(userId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.home_pinned_tickets),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (pinnedTickets.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_pinned_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                pinnedTickets.forEach { ticket ->
                    PinnedTicketRow(ticket = ticket, onClick = { onTicketClick(ticket.idTicket) })
                }
            }
        }
    }
}

@Composable
private fun PinnedTicketRow(ticket: Ticket, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.ticket_number_format, ticket.idTicket),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = ticket.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = ticket.priority.priorityName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun EmptyStateText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PermissionsCard(role: Role) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.home_permissions_title, role.name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            role.permits.forEach { permission ->
                Text(
                    text = "• ${permission.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun SectionTitle(title: String, action: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// MY TICKETS (CommonUser) / ASSIGNED (Operator)
// ---------------------------------------------------------------------------

@Composable
private fun TicketsContent(
    user: User,
    filters: TicketFilters,
    onFiltersChange: (TicketFilters) -> Unit,
    onTicketClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (user.role == Role.CommonUser) {
            TicketSearchBar(filters = filters, onFiltersChange = onFiltersChange)

            val myTickets = TicketRepository.searchTicketsByUser(user.idUser, filters)
            if (myTickets.isEmpty()) {
                EmptyStateText(
                    text = stringResource(
                        if (filters.isActive) R.string.tickets_no_results else R.string.tickets_empty
                    )
                )
            } else {
                myTickets.forEach { ticket ->
                    TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.idTicket) })
                }
            }
        } else {
            TicketCard()
        }
    }
}

// ---------------------------------------------------------------------------
// TEAM / SUMMARY / MANAGE (por construir)
// ---------------------------------------------------------------------------

@Composable
private fun PlaceholderContent(tab: BottomTab) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(tab.label),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Construction,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = stringResource(R.string.home_wip),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// PROFILE
// ---------------------------------------------------------------------------

@Composable
private fun ProfileContent(user: User, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${user.name} ${user.lastName}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.profile_role, user.role.name),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        PermissionsCard(role = user.role)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onLogoutClick) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(R.string.home_logout))
        }
    }
}
@Composable
private fun NewTicketButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = stringResource(R.string.home_new_ticket))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PanalSuiteTheme {
        HomeScreen(onLogoutClick = {})
    }
}