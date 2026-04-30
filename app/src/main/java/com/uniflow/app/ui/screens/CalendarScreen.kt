package com.uniflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uniflow.app.data.model.Course
import com.uniflow.app.data.model.ScheduleEntry
import com.uniflow.app.ui.auth.AuthViewModel

@Composable
fun CalendarScreen(
    adminViewModel: AdminViewModel = hiltViewModel(),
    lecturerViewModel: LecturerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val slots = listOf("Morning", "Afternoon")

    val userData by authViewModel.currentUserData.collectAsState()

    val isAdmin = userData?.role == "Admin"

    // Use derived state for entries to ensure proper observation
    val entries by (if (isAdmin) {
        adminViewModel.allScheduleEntries
    } else {
        lecturerViewModel.scheduleEntries
    }).collectAsState(initial = emptyList())

    val allCourses by adminViewModel.allCourses.collectAsState()

    LaunchedEffect(userData) {
        userData?.let { user ->
            // Update VM with latest username to trigger filtering
            lecturerViewModel.setLecturerId(user.username)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Weekly Schedule",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3), 
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item { Box(Modifier.size(40.dp)) }
            items(2) { index ->
                Text(
                    text = slots[index],
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }

            // Grid Rows
            days.forEachIndexed { dayIndex, day ->
                item {
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                
                repeat(2) { slotIndex ->
                    // Robust finding with Long/Int conversion safety
                    val entryInSlot = entries.find { 
                        it.day.toLong() == dayIndex.toLong() && it.timeSlot.toLong() == slotIndex.toLong() 
                    }
                    val course = allCourses.find { it.code == entryInSlot?.courseId || it.id == entryInSlot?.courseId }
                    
                    item {
                        CalendarSlot(entryInSlot, course)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSlot(entry: ScheduleEntry?, course: Course?) {
    Card(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry != null) Color(0xFF1A237E).copy(alpha = 0.1f) else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (entry != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = course?.code ?: entry.courseId,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        textAlign = TextAlign.Center
                    )
                    val courseName = course?.name ?: ""
                    Text(
                        text = if (courseName.length > 15) courseName.take(13) + ".." else courseName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = entry.classroomId,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
