package com.uniflow.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.*
import com.uniflow.app.data.repository.DataRepository
import com.uniflow.app.data.repository.ScheduleConflictException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AssignmentUiState {
    object Idle : AssignmentUiState()
    object Loading : AssignmentUiState()
    object Success : AssignmentUiState()
    data class Error(val message: String) : AssignmentUiState()
}

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val courses = repository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lecturers = repository.getAllLecturers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classrooms = repository.getAllClassrooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleEntries = repository.getAllScheduleEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<AssignmentUiState>(AssignmentUiState.Idle)
    val uiState: StateFlow<AssignmentUiState> = _uiState

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()

    fun submitAssignment(
        courseId: String,
        lecturerId: String,
        classroomId: String,
        day: Int,
        timeSlot: Int
    ) {
        if (courseId.isBlank() || lecturerId.isBlank() || classroomId.isBlank()) {
            viewModelScope.launch { _errorEvents.emit("Please fill all fields") }
            return
        }

        _uiState.value = AssignmentUiState.Loading
        viewModelScope.launch {
            try {
                val entry = ScheduleEntry(
                    courseId = courseId,
                    lecturerId = lecturerId,
                    classroomId = classroomId,
                    day = day,
                    timeSlot = timeSlot
                )
                repository.addScheduleEntry(entry)
                _uiState.value = AssignmentUiState.Success
            } catch (e: ScheduleConflictException) {
                _uiState.value = AssignmentUiState.Idle
                _errorEvents.emit(e.message ?: "Schedule conflict detected")
            } catch (e: Exception) {
                _uiState.value = AssignmentUiState.Error(e.message ?: "Assignment failed")
            }
        }
    }

    fun deleteAssignment(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteScheduleEntry(id)
            } catch (e: Exception) {
                _errorEvents.emit(e.message ?: "Deletion failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = AssignmentUiState.Idle
    }
}
