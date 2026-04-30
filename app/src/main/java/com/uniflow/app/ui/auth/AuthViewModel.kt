package com.uniflow.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniflow.app.data.model.User
import com.uniflow.app.data.repository.AuthRepository
import com.uniflow.app.data.repository.DataRepository
import com.uniflow.app.utils.HashUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataRepository: DataRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUserData: StateFlow<User?> = _currentUser.asStateFlow()

    fun login(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _loginState.value = AuthState.Error("Username and password cannot be empty")
            return
        }

        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val hashedPassword = HashUtils.sha256(pass)
                val user = authRepository.loginCustom(username.lowercase().trim(), hashedPassword)
                
                if (user != null) {
                    _currentUser.value = user
                    _loginState.value = AuthState.Success(user)
                } else {
                    _loginState.value = AuthState.Error("Invalid username or password")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun register(user: User, pass: String) {
        if (pass.isBlank()) {
            _loginState.value = AuthState.Error("Password cannot be empty")
            return
        }
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val passHash = HashUtils.sha256(pass)
                authRepository.register(user, passHash)
                val registeredUser = authRepository.getUserData(user.username)
                if (registeredUser != null) {
                    _currentUser.value = registeredUser
                    _loginState.value = AuthState.Success(registeredUser)
                } else {
                    _loginState.value = AuthState.Error("Registration failed - user data not found")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _loginState.value = AuthState.Idle
    }

    fun resetState() {
        _loginState.value = AuthState.Idle
    }
}
