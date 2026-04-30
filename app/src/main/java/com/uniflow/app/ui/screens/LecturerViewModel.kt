package com.uniflow.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.Lecturer
import com.uniflow.app.data.model.ScheduleEntry
import com.uniflow.app.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LecturerHomeUiState(
    val lecturer: Lecturer? = null,
    val departmentName: String = "",
    val weeklyCourseCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class LecturerViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LecturerHomeUiState())
    val uiState: StateFlow<LecturerHomeUiState> = _uiState

    private val _lecturerId = MutableStateFlow<String?>(null)
    private val _lecturerUsername = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val scheduleEntries: StateFlow<List<ScheduleEntry>> = combine(_lecturerId, _lecturerUsername) { id, username ->
        id to username
    }
    .filter { it.first != null || it.second != null }
    .flatMapLatest { (id, username) ->
        repository.getAllScheduleEntries().map { entries ->
            // Robust filtering using both id and username
            entries.filter { 
                (id != null && it.lecturerId == id) || 
                (username != null && it.lecturerId == username) 
            }
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses = repository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun initialize(lecturer: Lecturer) {
        if (_lecturerUsername.value == lecturer.username) return
        
        _lecturerId.value = lecturer.id
        _lecturerUsername.value = lecturer.username
        _uiState.update { it.copy(isLoading = true, lecturer = lecturer) }
        
        viewModelScope.launch {
            repository.getAllDepartments().collectLatest { departments ->
                // Robust department matching: compare trimmed and case-insensitive
                val target = lecturer.departmentId.trim()
                val dept = departments.find { 
                    it.id.equals(target, ignoreCase = true) || 
                    it.name.equals(target, ignoreCase = true) 
                }
                _uiState.update { it.copy(departmentName = dept?.name ?: "N/A") }
            }
        }

        viewModelScope.launch {
            scheduleEntries.collectLatest { entries ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        weeklyCourseCount = entries.size
                    )
                }
            }
        }
    }

    fun setLecturerId(id: String) {
        _lecturerId.value = id
    }
}
