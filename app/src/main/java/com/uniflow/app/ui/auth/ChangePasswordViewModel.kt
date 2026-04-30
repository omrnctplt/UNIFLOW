package com.uniflow.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.User
import com.uniflow.app.data.repository.AuthRepository
import com.uniflow.app.utils.HashUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val uiState: StateFlow<ChangePasswordState> = _uiState

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var currentUser: User? = null

    fun setUser(user: User) {
        currentUser = user
    }

    fun updatePassword(currentPass: String, newPass: String, confirmPass: String) {
        val user = currentUser ?: run {
            _uiState.value = ChangePasswordState.Error("User session not found")
            return
        }

        // Verify current password first
        val currentHash = HashUtils.sha256(currentPass)
        if (user.passwordHash != currentHash) {
            _uiState.value = ChangePasswordState.Error("Current password is incorrect")
            return
        }

        if (newPass.length < 6) {
            _uiState.value = ChangePasswordState.Error("Password must be at least 6 characters")
            return
        }

        if (newPass != confirmPass) {
            _uiState.value = ChangePasswordState.Error("Passwords do not match")
            return
        }

        _uiState.value = ChangePasswordState.Loading

        viewModelScope.launch {
            try {
                val newHash = HashUtils.sha256(newPass)
                authRepository.updatePassword(user.username, newHash)

                _uiState.value = ChangePasswordState.Success("Password updated successfully")
                _navigationEvent.emit(Unit)
            } catch (e: Exception) {
                _uiState.value = ChangePasswordState.Error(e.message ?: "Update failed")
            }
        }
    }
}
