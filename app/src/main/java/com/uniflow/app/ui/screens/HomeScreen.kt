package com.uniflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uniflow.app.data.model.User
import com.uniflow.app.data.model.UserRole
import com.uniflow.app.ui.auth.AuthViewModel
import com.uniflow.app.ui.auth.AuthState

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = hiltViewModel(), 
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val authState by authViewModel.loginState.collectAsState()
    
    val user = if (authState is AuthState.Success) (authState as AuthState.Success).user else null

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (user.role == UserRole.ADMIN.displayName) {
        AdminHomeContent(adminViewModel, user)
    } else {
        // LecturerHomeScreen expects a User or Lecturer? 
        // Let's call the real LecturerHomeScreen instead of the placeholder
        LecturerHomeScreen(user)
    }
}

@Composable
fun AdminHomeContent(adminViewModel: AdminViewModel, admin: User) {
    val allCourses by adminViewModel.allCourses.collectAsState()
    val allLecturers by adminViewModel.allLecturers.collectAsState()
    val allClassrooms by adminViewModel.allClassrooms.collectAsState()
    val allScheduleEntries by adminViewModel.allScheduleEntries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Welcome,",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(
            text = "Admin ${admin.name}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

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
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "System Administrator",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Management Console",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val unassignedLecturers = allLecturers.count { l -> allScheduleEntries.none { it.lecturerId == l.id || it.lecturerId == l.username } }
        val unassignedCourses = allCourses.count { c -> allScheduleEntries.none { it.courseId == c.id || it.courseId == c.code } }
        val availableClassrooms = allClassrooms.count { room ->
            allScheduleEntries.count { it.classroomId == room.id || it.classroomId == room.roomCode } < 10
        }

        AdminDashboard(unassignedLecturers, unassignedCourses, availableClassrooms)
    }
}

@Composable
fun AdminDashboard(unassignedLecturers: Int, unassignedCourses: Int, availableClassrooms: Int) {
    Text(
        text = "Dashboard Overview",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A237E)
    )
    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HomeStatCard("Unassigned\nLecturers", unassignedLecturers, Icons.Default.Warning, Modifier.weight(1f))
        HomeStatCard("Unassigned\nCourses", unassignedCourses, Icons.Default.Warning, Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HomeStatCard("Available\nClassrooms", availableClassrooms, Icons.Default.CheckCircle, Modifier.weight(1f))
    }
}

@Composable
fun HomeStatCard(label: String, value: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1A237E), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = label, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
        }
    }
}
