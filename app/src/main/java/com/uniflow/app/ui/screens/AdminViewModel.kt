package com.uniflow.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.*
import com.uniflow.app.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardMetrics(
    val unassignedLecturers: Int = 0,
    val unassignedCourses: Int = 0,
    val availableClassrooms: Int = 0
)

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            repository.getAllDepartments().first().let { depts ->
                if (depts.none { it.name == "Computer Engineering" }) {
                    repository.addDepartment(Department(name = "Computer Engineering"))
                }
            }
        }
    }

    val allClassrooms = repository.getAllClassrooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScheduleEntries = repository.getAllScheduleEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses = repository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLecturers = repository.getAllLecturers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDepartments = repository.getAllDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metrics: StateFlow<AdminDashboardMetrics> = combine(
        allLecturers,
        allCourses,
        allClassrooms,
        allScheduleEntries
    ) { lecturers, courses, classrooms, entries ->
        val unassignedLecturersCount = lecturers.count { lecturer ->
            entries.none { it.lecturerId == lecturer.id || it.lecturerId == lecturer.username }
        }
        val unassignedCoursesCount = courses.count { course ->
            entries.none { it.courseId == course.id || it.courseId == course.code }
        }
        val availableClassroomsCount = classrooms.count { classroom ->
            val usedSlots = entries.count { it.classroomId == classroom.id || it.classroomId == classroom.roomCode }
            usedSlots < 10
        }
        AdminDashboardMetrics(
            unassignedLecturers = unassignedLecturersCount,
            unassignedCourses = unassignedCoursesCount,
            availableClassrooms = availableClassroomsCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminDashboardMetrics())

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
                _addClassroomState.value = UiState.Error(e.message ?: "Derslik eklenemedi")
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
                _addAssignmentState.value = UiState.Error(e.message ?: "Atama başarısız")
            }
        }
    }

    fun resetAddClassroomState() { _addClassroomState.value = UiState.Idle }
    fun resetAddAssignmentState() { _addAssignmentState.value = UiState.Idle }
}
