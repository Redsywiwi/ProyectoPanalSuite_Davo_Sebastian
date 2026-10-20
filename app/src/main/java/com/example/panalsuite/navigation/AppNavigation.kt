package com.example.panalsuite.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.panalsuite.repository.Session
import com.example.panalsuite.repository.UserRepository
import com.example.panalsuite.ui.home.DrawerItem
import com.example.panalsuite.ui.home.HomeScreen
import com.example.panalsuite.ui.home.drawerItems
import com.example.panalsuite.ui.login.LoginScreen
import com.example.panalsuite.ui.tickets.NewTicketScreen
import com.example.panalsuite.ui.tickets.TicketDetailsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) }
    var loginError by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (Session.currentUser != null) Routes.HOME else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                errorMessage = loginError,
                testUsers = userRepository.getUsers(),
                onLoginClick = { email, password ->
                    val user = userRepository.authenticate(email.trim(), password)
                    if (user != null) {
                        Session.currentUser = user
                        loginError = null
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        loginError = "Wrong Email/Password"
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onLogoutClick = {
                    Session.currentUser = null
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onDrawerNavigate = { route -> navController.navigate(route) },
                onNewTicketClick = { navController.navigate(Routes.NEW_TICKET) },
                onTicketClick = { ticketId ->
                    navController.navigate(Routes.ticketDetails(ticketId))
                }
            )
        }
        composable(Routes.NEW_TICKET) {
            NewTicketScreen(
                onBackClick = { navController.popBackStack() },
                onTicketCreated = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.TICKET_DETAILS,
            arguments = listOf(navArgument(Routes.TICKET_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            TicketDetailsScreen(
                ticketId = backStackEntry.arguments?.getLong(Routes.TICKET_ID_ARG) ?: -1L,
                onBackClick = { navController.popBackStack() }
            )
        }
        drawerItems.forEach { item ->
            composable(item.route) {
                DrawerPlaceholderScreen(
                    item = item,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

// Pantalla temporal para las opciones del menu lateral
// asi no se siente tan vacio supong.
@Composable
private fun DrawerPlaceholderScreen(
    item: DrawerItem,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(item.label),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = item.route,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBackClick) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Volver")
        }
    }
}