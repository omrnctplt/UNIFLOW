package com.uniflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uniflow.app.data.model.Course
import com.uniflow.app.data.model.Lecturer
import com.uniflow.app.data.model.ScheduleEntry
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(viewModel: AssignmentViewModel = hiltViewModel()) {
    val courses by viewModel.courses.collectAsState()
    val lecturers by viewModel.lecturers.collectAsState()
    val classrooms by viewModel.classrooms.collectAsState()
    val scheduleEntries by viewModel.scheduleEntries.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedCourseId by remember { mutableStateOf("") }
    var selectedLecturerId by remember { mutableStateOf("") }
    var selectedClassroomId by remember { mutableStateOf("") }
    var selectedDay by remember { mutableIntStateOf(0) }
    var selectedTimeSlot by remember { mutableIntStateOf(0) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val timeSlots = listOf("Morning (08:00-12:00)", "Afternoon (13:00-17:00)")

    val snackbarHostState = remember { SnackbarHostState() }
    var entryToDelete by remember { mutableStateOf<ScheduleEntry?>(null) }

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AssignmentUiState.Success) {
            snackbarHostState.showSnackbar("Assignment saved successfully!")
            selectedCourseId = ""
            selectedLecturerId = ""
            selectedClassroomId = ""
            viewModel.resetState()
        }
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Assignment") },
            text = { Text("Are you sure you want to remove this assignment? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAssignment(entryToDelete!!.id)
                        entryToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Course Assignment",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Form Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Course Selection
                    DropdownSelector(
                        label = "Course",
                        options = courses.map { "${it.code} - ${it.name}" },
                        selectedOption = courses.find { it.id == selectedCourseId || it.code == selectedCourseId }?.let { "${it.code} - ${it.name}" } ?: "Select Course",
                        onOptionSelected = { index -> selectedCourseId = courses[index].id.ifEmpty { courses[index].code } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lecturer Selection
                    DropdownSelector(
                        label = "Lecturer",
                        options = lecturers.map { "${it.title} ${it.firstName} ${it.lastName}" },
                        selectedOption = lecturers.find { it.id == selectedLecturerId || it.username == selectedLecturerId }?.let { "${it.title} ${it.firstName} ${it.lastName}" } ?: "Select Lecturer",
                        onOptionSelected = { index -> selectedLecturerId = lecturers[index].id.ifEmpty { lecturers[index].username } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Classroom Selection
                    DropdownSelector(
                        label = "Classroom",
                        options = classrooms.map { "${it.roomCode} (Cap: ${it.capacity})" },
                        selectedOption = classrooms.find { it.id == selectedClassroomId || it.roomCode == selectedClassroomId }?.let { "${it.roomCode} (Cap: ${it.capacity})" } ?: "Select Classroom",
                        onOptionSelected = { index -> selectedClassroomId = classrooms[index].id.ifEmpty { classrooms[index].roomCode } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownSelector(
                                label = "Day",
                                options = days,
                                selectedOption = days[selectedDay],
                                onOptionSelected = { selectedDay = it }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownSelector(
                                label = "Time Slot",
                                options = timeSlots,
                                selectedOption = timeSlots[selectedTimeSlot],
                                onOptionSelected = { selectedTimeSlot = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.submitAssignment(selectedCourseId, selectedLecturerId, selectedClassroomId, selectedDay, selectedTimeSlot) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        enabled = uiState !is AssignmentUiState.Loading
                    ) {
                        if (uiState is AssignmentUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Assignment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Current Assignments",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of Existing Assignments
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scheduleEntries) { entry ->
                    val course = courses.find { it.id == entry.courseId || it.code == entry.courseId }
                    val lecturer = lecturers.find { it.id == entry.lecturerId || it.username == entry.lecturerId }
                    
                    AssignmentItem(
                        entry = entry, 
                        course = course, 
                        lecturer = lecturer, 
                        days = days, 
                        timeSlots = timeSlots,
                        onDelete = { entryToDelete = entry }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AssignmentItem(
    entry: ScheduleEntry,
    course: Course?,
    lecturer: Lecturer?,
    days: List<String>,
    timeSlots: List<String>,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = course?.code ?: "Unknown", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
            Text(text = course?.name ?: "Course Name", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lecturer: ${lecturer?.title} ${lecturer?.firstName} ${lecturer?.lastName}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
            Text(
                text = "${days[entry.day]} - ${timeSlots[entry.timeSlot]}",
                fontSize = 12.sp,
                color = Color(0xFF1A237E),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
