package com.example.panalsuite.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panalsuite.repository.SyncRepository
import com.example.panalsuite.ui.components.toastIfFailure
import com.example.panalsuite.viewmodel.LoginViewModel

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

    NavHost(
        navController = navController,
        startDestination = if (Session.currentUser != null) Routes.HOME else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel()
            val loginState by loginViewModel.state.collectAsState()

            // Cuando users-service acepta el login, se guarda la sesion y se navega al inicio
            LaunchedEffect(loginState.user) {
                val user = loginState.user ?: return@LaunchedEffect
                Session.currentUser = user
                loginViewModel.consumeLogin()
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }

            LoginScreen(
                errorMessage = loginState.error,
                testUsers = userRepository.getUsers(),
                onLoginClick = { email, password -> loginViewModel.login(email, password) }
            )
        }
        composable(Routes.HOME) {
            // Al entrar al inicio se descargan los datos de los microservicios
            LaunchedEffect(Unit) {
                SyncRepository.refreshAll().toastIfFailure(context)
            }
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