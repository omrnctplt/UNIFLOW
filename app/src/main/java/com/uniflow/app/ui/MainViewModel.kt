package com.uniflow.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val userName = userPreferences.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val userDepartment = userPreferences.userDepartment.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val userPosition = userPreferences.userPosition.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val userRole = userPreferences.userRole.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Lecturer")

    val isConfigured = combine(userName, userDepartment, userPosition) { name, dept, pos ->
        !name.isNullOrEmpty() && !dept.isNullOrEmpty() && !pos.isNullOrEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun saveSettings(name: String, dept: String, pos: String, role: String) {
        viewModelScope.launch {
            userPreferences.saveUserData(name, dept, pos, role)
        }
    }
}
