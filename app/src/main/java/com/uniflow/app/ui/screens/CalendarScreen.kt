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
fun CalendarScreen(viewModel: AdminViewModel = hiltViewModel(), authViewModel: AuthViewModel = hiltViewModel()) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val slots = listOf("Morning", "Afternoon")

    val allScheduleEntries by viewModel.allScheduleEntries.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val userData by authViewModel.currentUserData.collectAsState()

    // Filter entries for logged-in user if they are a Lecturer
    val entries = remember(allScheduleEntries, userData) {
        if (userData?.role == "Admin") {
            allScheduleEntries
        } else {
            allScheduleEntries.filter { it.lecturerId == userData?.username }
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
            columns = GridCells.Fixed(3), // Time slot + Day columns
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
                    val entryInSlot = entries.find { it.day == dayIndex && it.timeSlot == slotIndex }
                    val course = allCourses.find { it.courseCode == entryInSlot?.courseId || it.code == entryInSlot?.courseId }
                    
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
                        text = course?.courseCode ?: entry.courseId,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = course?.name ?: "",
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
