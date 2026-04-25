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
import com.uniflow.app.ui.auth.AuthViewModel

@Composable
fun HomeScreen(viewModel: AuthViewModel = hiltViewModel(), adminViewModel: AdminViewModel = hiltViewModel()) {
    val userData by viewModel.currentUserData.collectAsState()

    // For Admin Dashboard
    val allCourses by adminViewModel.allCourses.collectAsState()
    val allLecturers by adminViewModel.allLecturers.collectAsState()
    val allClassrooms by adminViewModel.allClassrooms.collectAsState()
    val allScheduleEntries by adminViewModel.allScheduleEntries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val titleText = if (userData?.role == "Admin") "Admin" else "${userData?.title} ${userData?.name} ${userData?.surname}".trim()
        Text(
            text = "Welcome,",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(
            text = titleText,
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
                        text = userData?.department ?: "No Department",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = userData?.position ?: "No Position",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (userData?.role == "Admin") {
            val unassignedLecturers = allLecturers.count { lecturer -> allScheduleEntries.none { it.lecturerId == lecturer.username } }
            val unassignedCourses = allCourses.count { course -> allScheduleEntries.none { it.courseId == course.courseCode || it.courseId == course.code } }

            // Available classrooms: total classrooms minus classrooms that are fully booked for all 10 slots (5 days * 2 slots)
            // Or simpler for this scope: count classrooms with at least one free slot (which is practically all unless completely full)
            // A more strict interpretation: no entries at all, but the prompt says "No entries, or at least one free slot left".
            // Since max slots is 10, a classroom is available if it has < 10 entries.
            val availableClassrooms = allClassrooms.count { classroom ->
                allScheduleEntries.count { it.classroomId == classroom.roomCode } < 10
            }

            AdminDashboard(unassignedLecturers, unassignedCourses, availableClassrooms)
        } else {
            val assignedEntries = allScheduleEntries.count { it.lecturerId == userData?.username }
            LecturerDashboard(assignedEntries)
        }
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
        StatCard("Unassigned\nLecturers", unassignedLecturers, Icons.Default.Warning, Modifier.weight(1f))
        StatCard("Unassigned\nCourses", unassignedCourses, Icons.Default.Warning, Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StatCard("Available\nClassrooms", availableClassrooms, Icons.Default.CheckCircle, Modifier.weight(1f))
    }
}

@Composable
fun LecturerDashboard(assignedEntries: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weekly Summary", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total assigned courses for the current week: $assignedEntries", color = Color.Gray)
        }
    }
}
