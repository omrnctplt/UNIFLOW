package com.uniflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uniflow.app.data.model.ScheduleEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val courses by viewModel.allCourses.collectAsState()
    val lecturers by viewModel.allLecturers.collectAsState()
    val classrooms by viewModel.allClassrooms.collectAsState()
    val scheduleEntries by viewModel.allScheduleEntries.collectAsState()
    val addState by viewModel.addAssignmentState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedCourseId by remember { mutableStateOf("") }
    var selectedLecturerId by remember { mutableStateOf("") }
    var selectedClassroomId by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf(0) }
    var selectedTimeSlot by remember { mutableStateOf(0) }

    var courseExpanded by remember { mutableStateOf(false) }
    var lecturerExpanded by remember { mutableStateOf(false) }
    var classroomExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val timeSlots = listOf("Morning", "Afternoon")

    LaunchedEffect(addState) {
        when (addState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Assignment added successfully!")
                viewModel.resetAddAssignmentState()
                selectedCourseId = ""
                selectedLecturerId = ""
                selectedClassroomId = ""
                selectedDay = 0
                selectedTimeSlot = 0
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((addState as UiState.Error).message)
                viewModel.resetAddAssignmentState()
            }
            else -> {}
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Assignment") },
            text = { Text("Are you sure you want to assign ${selectedCourseId} to ${selectedLecturerId} in room ${selectedClassroomId} on ${days[selectedDay]} (${timeSlots[selectedTimeSlot]})?") },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    viewModel.addAssignment(selectedCourseId, selectedLecturerId, selectedClassroomId, selectedDay, selectedTimeSlot)
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Assign Course", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ExposedDropdownMenuBox(
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = !courseExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCourseId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = courseExpanded,
                        onDismissRequest = { courseExpanded = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text("${course.courseCode} - ${course.name}") },
                                onClick = {
                                    selectedCourseId = course.courseCode
                                    courseExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = lecturerExpanded,
                    onExpandedChange = { lecturerExpanded = !lecturerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLecturerId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lecturer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lecturerExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = lecturerExpanded,
                        onDismissRequest = { lecturerExpanded = false }
                    ) {
                        lecturers.forEach { lecturer ->
                            DropdownMenuItem(
                                text = { Text("${lecturer.name} ${lecturer.surname} (${lecturer.username})") },
                                onClick = {
                                    selectedLecturerId = lecturer.username
                                    lecturerExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = classroomExpanded,
                    onExpandedChange = { classroomExpanded = !classroomExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedClassroomId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Classroom") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classroomExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = classroomExpanded,
                        onDismissRequest = { classroomExpanded = false }
                    ) {
                        classrooms.forEach { room ->
                            DropdownMenuItem(
                                text = { Text(room.roomCode) },
                                onClick = {
                                    selectedClassroomId = room.roomCode
                                    classroomExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = dayExpanded,
                        onExpandedChange = { dayExpanded = !dayExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (selectedDay in days.indices) days[selectedDay] else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            days.forEachIndexed { index, day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        selectedDay = index
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = timeExpanded,
                        onExpandedChange = { timeExpanded = !timeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (selectedTimeSlot in timeSlots.indices) timeSlots[selectedTimeSlot] else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Time Slot") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = timeExpanded,
                            onDismissRequest = { timeExpanded = false }
                        ) {
                            timeSlots.forEachIndexed { index, slot ->
                                DropdownMenuItem(
                                    text = { Text(slot) },
                                    onClick = {
                                        selectedTimeSlot = index
                                        timeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        if (selectedCourseId.isNotBlank() && selectedLecturerId.isNotBlank() && selectedClassroomId.isNotBlank()) {
                            showConfirmDialog = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill in all assignment fields.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                ) {
                    Text("Save Assignment")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Existing Assignments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Spacer(modifier = Modifier.height(8.dp))

                // Existing Assignments List
                if (scheduleEntries.isEmpty()) {
                    Text("No assignments yet.", color = Color.Gray)
                } else {
                    scheduleEntries.forEach { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${entry.courseId} - ${entry.classroomId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Lecturer: ${entry.lecturerId}", fontSize = 12.sp, color = Color.DarkGray)
                                Text("${days.getOrNull(entry.day) ?: "Unknown Day"} - ${timeSlots.getOrNull(entry.timeSlot) ?: "Unknown Time"}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
