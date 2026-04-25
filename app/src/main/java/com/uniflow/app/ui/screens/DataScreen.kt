package com.uniflow.app.ui.screens

import android.content.Context
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.uniflow.app.data.model.Course
import com.uniflow.app.data.model.User
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val importedCourses by viewModel.importedCourses.collectAsState()
    val coursesCount by viewModel.coursesCount.collectAsState()
    val lecturersCount by viewModel.lecturersCount.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val allLecturers by viewModel.allLecturers.collectAsState()
    val newCourseCodes by viewModel.newCourseCodes.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedLecturer by remember { mutableStateOf<User?>(null) }
    var showLecturerDetails by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importExcel(context, it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Data Management", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E)) },
                actions = {
                    Button(
                        onClick = { launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Import Excel", fontSize = 12.sp)
                    }
                    if (importedCourses.isNotEmpty()) {
                        IconButton(onClick = { viewModel.confirmUpload() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color(0xFF1A237E))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
        ) {
            // Stats Row (Videodaki gibi yan yana iki kart)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Courses",
                    count = coursesCount,
                    icon = Icons.Default.AutoStories,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Lecturers",
                    count = lecturersCount,
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab View
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Directory", "Faculty Members")
            
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1A237E),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF1A237E)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Empty State Control
            if (allCourses.isEmpty() && allLecturers.isEmpty() && importedCourses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AutoStories, 
                            contentDescription = null, 
                            modifier = Modifier.size(100.dp), 
                            tint = Color.LightGray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No data imported yet", color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Click 'Import Excel' to start managing your courses", 
                            fontSize = 12.sp, 
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedTab == 0) {
                        // Directory View
                        items(allCourses) { course ->
                            CourseListItem(course)
                        }
                    } else {
                        // Faculty View
                        items(allLecturers) { lecturer ->
                            LecturerListItem(lecturer) {
                                selectedLecturer = lecturer
                                showLecturerDetails = true
                            }
                        }
                    }
                }
            }
        }
    }

    // Preview Dialog
    if (uiState is DataUiState.Preview) {
        ExcelPreviewDialog(
            courses = importedCourses,
            newCodes = newCourseCodes,
            onConfirm = { viewModel.confirmUpload() },
            onCancel = { viewModel.clearPreview() }
        )
    }

    // Modal Details
    if (showLecturerDetails && selectedLecturer != null) {
        LecturerDetailsDialog(
            lecturer = selectedLecturer!!,
            courses = allCourses.filter { it.lecturerId == selectedLecturer!!.username },
            onDismiss = { showLecturerDetails = false }
        )
    }

    // Loading Overlay
    if (uiState is DataUiState.Loading) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1A237E))
                    Spacer(Modifier.height(12.dp))
                    Text("Processing...", fontSize = 12.sp, color = Color(0xFF1A237E))
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is DataUiState.Success) {
            snackbarHostState.showSnackbar("Data synced successfully!")
        } else if (uiState is DataUiState.Error) {
            snackbarHostState.showSnackbar("Error: ${(uiState as DataUiState.Error).message}")
        }
    }
}

@Composable
fun CourseListItem(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = course.courseCode,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                modifier = Modifier.width(80.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = course.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = "Lecturer: ${course.lecturerId}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun LecturerListItem(lecturer: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1A237E)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = lecturer.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${lecturer.name} ${lecturer.surname}", fontWeight = FontWeight.SemiBold)
                Text(text = lecturer.position, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun StatCard(title: String, count: Int, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1A237E), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ExcelPreviewDialog(
    courses: List<Course>,
    newCodes: Set<String>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Excel Preview (First 3 Rows)", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CODE", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("COURSE NAME", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
                    Text("STATUS", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }
                
                courses.take(3).forEach { course ->
                    val isNew = course.courseCode in newCodes
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(course.courseCode, fontSize = 10.sp, modifier = Modifier.weight(1f))
                        Text(course.name, fontSize = 10.sp, modifier = Modifier.weight(2f))
                        Text(
                            text = if (isNew) "NEW" else "UPDATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNew) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                }

                if (courses.size > 3) {
                    Text(
                        text = "... and ${courses.size - 3} more rows",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel", color = Color.Red) }
        }
    )
}

@Composable
fun LecturerDetailsDialog(lecturer: User, courses: List<Course>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF1A237E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = lecturer.name.take(1).uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "${lecturer.name} ${lecturer.surname}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = lecturer.position, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Assigned Courses", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(courses) { course ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(course.courseCode, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), modifier = Modifier.width(70.dp), fontSize = 13.sp)
                            Text(course.name, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))) {
                    Text("Close")
                }
            }
        }
    }
}

fun downloadSampleExcel(context: Context) {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Courses")
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("Course Code")
    headerRow.createCell(1).setCellValue("Course Name")
    headerRow.createCell(2).setCellValue("Lecturer Name")
    headerRow.createCell(3).setCellValue("Department")

    val sampleRow = sheet.createRow(1)
    sampleRow.createCell(0).setCellValue("CSE101")
    sampleRow.createCell(1).setCellValue("Introduction to Programming")
    sampleRow.createCell(2).setCellValue("Prof. Dr. John Doe")
    sampleRow.createCell(3).setCellValue("Computer Engineering")

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "UniFlow_Sample.xlsx")
    try {
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
    } catch (e: Exception) { e.printStackTrace() }
}
