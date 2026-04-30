package com.uniflow.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomsScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val classrooms by viewModel.allClassrooms.collectAsState()
    val departments by viewModel.allDepartments.collectAsState()
    val addState by viewModel.addClassroomState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var selectedDeptId by remember { mutableStateOf("") }
    var deptExpanded by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(addState) {
        when (addState) {
            is UiState.Success -> {
                showAddDialog = false
                roomCode = ""
                capacity = ""
                selectedDeptId = ""
                viewModel.resetAddClassroomState()
                snackbarHostState.showSnackbar("Derslik başarıyla eklendi!")
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((addState as UiState.Error).message)
                viewModel.resetAddClassroomState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF1A237E)) {
                Icon(Icons.Default.Add, contentDescription = "Derslik Ekle", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Derslikler", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(16.dp))

            if (classrooms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz derslik eklenmemiş.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(classrooms) { classroom ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(classroom.roomCode, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    val deptName = departments.find { it.id == classroom.departmentId }?.name ?: "Bilinmeyen Bölüm"
                                    Text(deptName, fontSize = 12.sp, color = Color.Gray)
                                }
                                Text("Kapasite: ${classroom.capacity}", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Derslik Ekle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = roomCode,
                        onValueChange = { roomCode = it },
                        label = { Text("Oda Kodu (örn: A-101)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Kapasite") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = deptExpanded,
                        onExpandedChange = { deptExpanded = !deptExpanded }
                    ) {
                        val selectedDeptName = departments.find { it.id == selectedDeptId }?.name ?: "Bölüm Seçin"
                        OutlinedTextField(
                            value = selectedDeptName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bölüm") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false }
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.name) },
                                    onClick = {
                                        selectedDeptId = dept.id
                                        deptExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = capacity.toIntOrNull() ?: 0
                        if (roomCode.isNotBlank() && selectedDeptId.isNotBlank()) {
                            viewModel.addClassroom(roomCode, cap, selectedDeptId)
                        }
                    },
                    enabled = addState !is UiState.Loading
                ) {
                    if (addState is UiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("İptal") }
            }
        )
    }
}
