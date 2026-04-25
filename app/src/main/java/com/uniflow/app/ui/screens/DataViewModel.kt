package com.uniflow.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.Course
import com.uniflow.app.data.model.User
import com.uniflow.app.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.WorkbookFactory
import javax.inject.Inject

sealed class DataUiState {
    object Idle : DataUiState()
    object Loading : DataUiState()
    object Preview : DataUiState()
    object Success : DataUiState()
    data class Error(val message: String) : DataUiState()
}

@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DataUiState>(DataUiState.Idle)
    val uiState: StateFlow<DataUiState> = _uiState

    private val _importedCourses = MutableStateFlow<List<Course>>(emptyList())
    val importedCourses: StateFlow<List<Course>> = _importedCourses

    private val _importedLecturers = MutableStateFlow<List<User>>(emptyList())
    val importedLecturers: StateFlow<List<User>> = _importedLecturers

    // Track which course codes are new vs existing
    private val _newCourseCodes = MutableStateFlow<Set<String>>(emptySet())
    val newCourseCodes: StateFlow<Set<String>> = _newCourseCodes

    val coursesCount = repository.getCoursesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lecturersCount = repository.getLecturersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allCourses = repository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLecturers = repository.getAllLecturers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun importExcel(context: Context, uri: Uri) {
        _uiState.value = DataUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)
                    val coursesMap = mutableMapOf<String, Course>()
                    val lecturersMap = mutableMapOf<String, User>()

                    for (row in sheet) {
                        if (row.rowNum == 0) continue // Skip header

                        val code = row.getCell(0)?.toString()?.trim() ?: ""
                        val name = row.getCell(1)?.toString()?.trim() ?: ""
                        val lecturerName = row.getCell(2)?.toString()?.trim() ?: ""
                        val dept = row.getCell(3)?.toString()?.trim() ?: ""

                        if (code.isNotEmpty()) {
                            // Smart Deduplication: If multiple rows have the same code, we take the last one or first one. 
                            // Here we use map to ensure uniqueness by code.
                            val lecturer = generateLecturerInfo(lecturerName, dept)
                            lecturersMap[lecturer.username] = lecturer
                            coursesMap[code] = Course(
                                courseCode = code,
                                name = name,
                                department = dept,
                                lecturerId = lecturer.username
                            )
                        }
                    }
                    
                    val uniqueCourses = coursesMap.values.toList()
                    val existingCodes = allCourses.value.map { it.courseCode }.toSet()
                    
                    _newCourseCodes.value = uniqueCourses
                        .filter { it.courseCode !in existingCodes }
                        .map { it.courseCode }
                        .toSet()

                    _importedCourses.value = uniqueCourses
                    _importedLecturers.value = lecturersMap.values.toList()
                    _uiState.value = DataUiState.Preview
                    workbook.close()
                }
            } catch (e: Exception) {
                _uiState.value = DataUiState.Error(e.message ?: "Failed to parse Excel")
            }
        }
    }

    fun confirmUpload() {
        _uiState.value = DataUiState.Loading
        viewModelScope.launch {
            try {
                repository.saveImportedData(_importedCourses.value, _importedLecturers.value)
                _uiState.value = DataUiState.Success
                clearPreview()
            } catch (e: Exception) {
                _uiState.value = DataUiState.Error(e.message ?: "Failed to save data")
            }
        }
    }

    fun clearPreview() {
        _importedCourses.value = emptyList()
        _importedLecturers.value = emptyList()
        _newCourseCodes.value = emptySet()
        _uiState.value = DataUiState.Idle
    }

    private fun generateLecturerInfo(fullName: String, department: String): User {
        val titles = listOf("Prof. Dr.", "Assoc. Prof.", "Asst. Prof.", "Dr.", "Lect.", "Prof.")
        var cleanedName = fullName
        titles.forEach { title ->
            cleanedName = cleanedName.replace(title, "", ignoreCase = true)
        }
        
        val username = cleanedName.trim().lowercase().replace(" ", "_")
            .replace("ı", "i").replace("ğ", "g").replace("ü", "u")
            .replace("ş", "s").replace("ö", "o").replace("ç", "c")
            
        val password = (100000..999999).random().toString()
        
        return User(
            name = cleanedName.trim(),
            username = username,
            password = password,
            department = department,
            role = "Lecturer",
            position = "Lecturer"
        )
    }
}
