package com.uniflow.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.Course
import com.uniflow.app.data.model.Lecturer
import com.uniflow.app.data.model.UserRole
import com.uniflow.app.data.repository.DataRepository
import com.uniflow.app.utils.HashUtils
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

    private val _importedLecturers = MutableStateFlow<List<Lecturer>>(emptyList())
    val importedLecturers: StateFlow<List<Lecturer>> = _importedLecturers

    private val _generatedCredentials = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val generatedCredentials: StateFlow<List<Pair<String, String>>> = _generatedCredentials

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
                    val lecturersMap = mutableMapOf<String, Lecturer>()
                    val credentialsMap = mutableMapOf<String, String>()

                    for (row in sheet) {
                        if (row.rowNum == 0) continue // Skip header

                        val code = row.getCell(0)?.toString()?.trim() ?: ""
                        val name = row.getCell(1)?.toString()?.trim() ?: ""
                        val lecturerName = row.getCell(2)?.toString()?.trim() ?: ""
                        val dept = row.getCell(3)?.toString()?.trim() ?: ""

                        // 1. Bağımsız Ders İçe Aktarma (Hoca ataması boş)
                        if (code.isNotEmpty()) {
                            coursesMap[code] = Course(
                                code = code,
                                name = name,
                                departmentId = dept,
                                lecturerId = "" // Atama manuel yapılacak
                            )
                        }

                        // 2. Bağımsız Hoca İçe Aktarma (Eğer isim varsa ve daha önce eklenmemişse)
                        if (lecturerName.isNotEmpty()) {
                            val tempUsername = generateUsername(lecturerName)
                            if (!lecturersMap.containsKey(tempUsername)) {
                                val info = generateLecturerInfoWithPass(lecturerName, dept)
                                lecturersMap[tempUsername] = info.first
                                credentialsMap[tempUsername] = info.second
                            }
                        }
                    }
                    
                    val uniqueCourses = coursesMap.values.toList()
                    val existingCodes = allCourses.value.map { it.code }.toSet()
                    
                    _newCourseCodes.value = uniqueCourses
                        .filter { it.code !in existingCodes }
                        .map { it.code }
                        .toSet()

                    _importedCourses.value = uniqueCourses
                    _importedLecturers.value = lecturersMap.values.toList()
                    _generatedCredentials.value = credentialsMap.toList()
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
                _importedCourses.value = emptyList()
                _importedLecturers.value = emptyList()
                _newCourseCodes.value = emptySet()
            } catch (e: Exception) {
                _uiState.value = DataUiState.Error(e.message ?: "Failed to save data")
            }
        }
    }

    fun clearCredentials() {
        _generatedCredentials.value = emptyList()
        _uiState.value = DataUiState.Idle
    }

    fun clearPreview() {
        _importedCourses.value = emptyList()
        _importedLecturers.value = emptyList()
        _newCourseCodes.value = emptySet()
        _generatedCredentials.value = emptyList()
        _uiState.value = DataUiState.Idle
    }

    private fun generateUsername(fullName: String): String {
        val titles = listOf("Prof. Dr.", "Assoc. Prof.", "Asst. Prof.", "Dr.", "Lect.", "Prof.", "Assoc.")
        var cleanedName = fullName
        for (title in titles) {
            cleanedName = cleanedName.replace(title, "", ignoreCase = true)
        }
        return cleanedName.trim()
            .replace("ı", "i").replace("I", "i")
            .replace("ğ", "g").replace("Ğ", "g")
            .replace("ü", "u").replace("Ü", "u")
            .replace("ş", "s").replace("Ş", "s")
            .replace("ö", "o").replace("Ö", "o")
            .replace("ç", "c").replace("Ç", "c")
            .lowercase()
            .replace(" ", "_")
    }

    private fun generateLecturerInfoWithPass(fullName: String, department: String): Pair<Lecturer, String> {
        val titles = listOf("Prof. Dr.", "Assoc. Prof.", "Asst. Prof.", "Dr.", "Lect.", "Prof.", "Assoc.")
        var cleanedName = fullName
        var foundTitle = ""

        for (title in titles) {
            if (cleanedName.contains(title, ignoreCase = true)) {
                foundTitle = title
                cleanedName = cleanedName.replace(title, "", ignoreCase = true)
            }
        }
        
        val username = generateUsername(fullName)
            
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val rawPassword = (1..6).map { chars.random() }.joinToString("")
        val passwordHash = HashUtils.sha256(rawPassword)
        
        val nameParts = cleanedName.trim().split(" ")
        val firstName = if (nameParts.size > 1) nameParts.dropLast(1).joinToString(" ") else nameParts.firstOrNull() ?: ""
        val lastName = if (nameParts.size > 1) nameParts.last() else ""

        val lecturer = Lecturer(
            id = username,
            title = foundTitle.trim(),
            firstName = firstName,
            lastName = lastName,
            departmentId = department,
            username = username,
            passwordHash = passwordHash,
            mustChangePassword = true,
            role = UserRole.LECTURER.displayName
        )
        return lecturer to rawPassword
    }
}
