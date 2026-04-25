package com.uniflow.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.uniflow.app.ui.auth.AuthViewModel
import com.uniflow.app.ui.auth.AuthState
import com.uniflow.app.ui.auth.ChangePasswordScreen
import com.uniflow.app.ui.auth.LoginScreen
import com.uniflow.app.ui.auth.RegisterScreen
import com.uniflow.app.ui.navigation.Screen
import com.uniflow.app.ui.screens.*

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    val userData by authViewModel.currentUserData.collectAsState()
    
    val adminItems = listOf(
        Screen.Home,
        Screen.Calendar,
        Screen.Data,
        Screen.Classrooms,
        Screen.Assignments,
        Screen.Settings
    )

    val lecturerItems = listOf(
        Screen.Home,
        Screen.Calendar,
        Screen.Settings
    )

    val navItems = remember(userData) {
        if (userData?.role == "Admin") adminItems else lecturerItems
    }

    // Auth redirection logic
    LaunchedEffect(authState, userData) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (userData != null) {
                    if (userData!!.mustChangePassword) {
                        if (navController.currentDestination?.route != Screen.ChangePassword.route) {
                            navController.navigate(Screen.ChangePassword.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else if (!userData!!.onboarded) {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute == Screen.Login.route || currentRoute == Screen.Register.route || currentRoute == Screen.Onboarding.route || currentRoute == Screen.ChangePassword.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }
            is AuthState.Idle -> {
                // Logout sonrası burası tetiklenir
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    if (authState is AuthState.Loading && userData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Using navItems from above logic
                val showBottomBar = navItems.any { it.route == currentRoute } &&
                                   userData != null && 
                                   userData!!.onboarded

                if (showBottomBar) {
                    NavigationBar {
                        val currentDestination = navBackStackEntry?.destination
                        navItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { screen.icon?.let { Icon(it, contentDescription = null) } },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController, 
                startDestination = Screen.Login.route, 
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = { }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = { }
                    )
                }
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(onFinish = {
                        authViewModel.completeOnboarding()
                    })
                }
                composable(Screen.ChangePassword.route) {
                    ChangePasswordScreen(
                        viewModel = authViewModel,
                        onChangeSuccess = {
                            // Handled by LaunchedEffect in MainScreen
                        }
                    )
                }
                composable(Screen.Home.route) { HomeScreen(authViewModel) }
                composable(Screen.Calendar.route) { CalendarScreen() }
                composable(Screen.Data.route) { DataScreen() }
                composable(Screen.Classrooms.route) { ClassroomsScreen() }
                composable(Screen.Assignments.route) { AssignmentScreen() }
                composable(Screen.Settings.route) { SettingsScreen(authViewModel) }
            }
        }
    }
}
