package com.example.panalsuite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.panalsuite.repository.Session
import com.example.panalsuite.repository.UserRepository
import com.example.panalsuite.ui.home.HomeScreen
import com.example.panalsuite.ui.login.LoginScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) }
    var loginError by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                errorMessage = loginError,
                onLoginClick = { email, password ->
                    val user = userRepository.authenticate(email.trim(), password)
                    if (user != null) {
                        Session.currentUser = user
                        loginError = null
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        loginError = "Correo o contraseña incorrectos"
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
                }
            )
        }
    }
}