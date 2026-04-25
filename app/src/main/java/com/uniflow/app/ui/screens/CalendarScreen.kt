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

@Composable
fun CalendarScreen(viewModel: DataViewModel = hiltViewModel()) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val slots = listOf("Morning", "Afternoon")
    val courses by viewModel.importedCourses.collectAsState() // In real app, fetch from DB

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
                    val slotId = dayIndex * 2 + slotIndex
                    val courseInSlot = courses.find { it.slot == slotId }
                    
                    item {
                        CalendarSlot(courseInSlot)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSlot(course: Course?) {
    Card(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (course != null) Color(0xFF1A237E).copy(alpha = 0.1f) else Color.Transparent
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
            if (course != null) {
                Text(
                    text = course.courseCode,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
