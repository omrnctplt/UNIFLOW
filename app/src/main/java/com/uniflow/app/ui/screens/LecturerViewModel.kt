package com.uniflow.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.ScheduleEntry
import com.uniflow.app.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LecturerViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _lecturerId = MutableStateFlow<String?>(null)

    val scheduleEntries: StateFlow<List<ScheduleEntry>> = _lecturerId
        .filterNotNull()
        .flatMapLatest { id ->
            repository.getScheduleEntriesForLecturer(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLecturerId(id: String) {
        _lecturerId.value = id
    }
}
