package com.uniflow.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.Classroom
import com.uniflow.app.data.model.Course
import com.uniflow.app.data.model.ScheduleEntry
import com.uniflow.app.data.model.User
import com.uniflow.app.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val allClassrooms = repository.getAllClassrooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScheduleEntries = repository.getAllScheduleEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses = repository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLecturers = repository.getAllLecturers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _addClassroomState = MutableStateFlow<UiState>(UiState.Idle)
    val addClassroomState: StateFlow<UiState> = _addClassroomState

    private val _addAssignmentState = MutableStateFlow<UiState>(UiState.Idle)
    val addAssignmentState: StateFlow<UiState> = _addAssignmentState

    fun addClassroom(roomCode: String, capacity: Int, departmentId: String) {
        _addClassroomState.value = UiState.Loading
        viewModelScope.launch {
            try {
                repository.addClassroom(Classroom(roomCode = roomCode, capacity = capacity, departmentId = departmentId))
                _addClassroomState.value = UiState.Success
            } catch (e: Exception) {
                _addClassroomState.value = UiState.Error(e.message ?: "Failed to add classroom")
            }
        }
    }

    fun addAssignment(courseId: String, lecturerId: String, classroomId: String, day: Int, timeSlot: Int) {
        _addAssignmentState.value = UiState.Loading
        viewModelScope.launch {
            try {
                repository.addScheduleEntry(
                    ScheduleEntry(
                        courseId = courseId,
                        lecturerId = lecturerId,
                        classroomId = classroomId,
                        day = day,
                        timeSlot = timeSlot
                    )
                )
                _addAssignmentState.value = UiState.Success
            } catch (e: Exception) {
                _addAssignmentState.value = UiState.Error(e.message ?: "Failed to add assignment")
            }
        }
    }

    fun resetAddClassroomState() { _addClassroomState.value = UiState.Idle }
    fun resetAddAssignmentState() { _addAssignmentState.value = UiState.Idle }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}
