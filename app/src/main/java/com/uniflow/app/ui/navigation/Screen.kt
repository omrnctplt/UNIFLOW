package com.uniflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Data : Screen("data", "Data", Icons.Default.Storage)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Classrooms : Screen("classrooms", "Classrooms", Icons.Default.Storage) // We'll use a standard icon like Storage or Domain if available. Using Storage for now.
    object Assignments : Screen("assignments", "Assignments", Icons.Default.CalendarMonth) // Reusing CalendarMonth
    object ChangePassword : Screen("change_password")
}
