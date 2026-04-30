package com.uniflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uniflow.app.data.model.Lecturer
import com.uniflow.app.data.model.User
import com.uniflow.app.data.model.UserRole

@Composable
fun LecturerHomeScreen(
    user: User,
    viewModel: LecturerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Map User to Lecturer for initialization
    val lecturer = remember(user) {
        Lecturer(
            id = user.id,
            title = user.title,
            firstName = user.firstName,
            lastName = user.lastName,
            departmentId = user.departmentId,
            username = user.username,
            passwordHash = user.passwordHash,
            mustChangePassword = user.mustChangePassword,
            role = user.role
        )
    }

    LaunchedEffect(lecturer) {
        viewModel.initialize(lecturer)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1A237E))
            }
        } else {
            Text(
                text = "Welcome,",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "${user.title} ${user.firstName} ${user.lastName}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Department Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Department", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(text = uiState.departmentName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1A237E).copy(alpha = 0.1f)
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF1A237E), modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Weekly Schedule", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "Total assigned courses: ${uiState.weeklyCourseCount}",
                            color = Color(0xFF1A237E),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
