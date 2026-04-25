package com.uniflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val courses by viewModel.allCourses.collectAsState()
    val lecturers by viewModel.allLecturers.collectAsState()
    val classrooms by viewModel.allClassrooms.collectAsState()
    val addState by viewModel.addAssignmentState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedCourseId by remember { mutableStateOf("") }
    var selectedLecturerId by remember { mutableStateOf("") }
    var selectedClassroomId by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf(0) }
    var selectedTimeSlot by remember { mutableStateOf(0) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val timeSlots = listOf("Morning", "Afternoon")

    LaunchedEffect(addState) {
        when (addState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Assignment added successfully!")
                viewModel.resetAddAssignmentState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((addState as UiState.Error).message)
                viewModel.resetAddAssignmentState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Assign Course", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(16.dp))

            // Very basic dropdown equivalents using OutlinedTextField for this prototype
            // In a real app, use ExposedDropdownMenuBox

            OutlinedTextField(
                value = selectedCourseId,
                onValueChange = { selectedCourseId = it },
                label = { Text("Course ID (e.g. CSE101)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedLecturerId,
                onValueChange = { selectedLecturerId = it },
                label = { Text("Lecturer Username (e.g. halit_bakir)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedClassroomId,
                onValueChange = { selectedClassroomId = it },
                label = { Text("Classroom ID (e.g. ROOM1)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedDay.toString(),
                onValueChange = { selectedDay = it.toIntOrNull() ?: 0 },
                label = { Text("Day (0-4 for Mon-Fri)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = selectedTimeSlot.toString(),
                onValueChange = { selectedTimeSlot = it.toIntOrNull() ?: 0 },
                label = { Text("Time Slot (0=Morning, 1=Afternoon)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.addAssignment(selectedCourseId, selectedLecturerId, selectedClassroomId, selectedDay, selectedTimeSlot)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Assignment")
            }
        }
    }
}
