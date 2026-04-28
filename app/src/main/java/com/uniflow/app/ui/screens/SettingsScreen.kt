package com.uniflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uniflow.app.ui.auth.AuthViewModel

@Composable
fun SettingsScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val userData by viewModel.currentUserData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Account Info", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1A237E))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${userData?.name} ${userData?.surname}")
                Text("Username: ${userData?.username}")
                Text("Role: ${userData?.role}")
                Text("Dept: ${userData?.department}")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        var showLogoutDialog by remember { mutableStateOf(false) }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    Button(onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Logout", color = Color.White)
        }
    }
}
