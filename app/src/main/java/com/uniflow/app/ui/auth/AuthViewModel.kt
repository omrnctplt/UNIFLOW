package com.uniflow.app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.uniflow.app.data.model.User
import com.uniflow.app.data.repository.AuthRepository
import com.uniflow.app.utils.PasswordValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData: StateFlow<User?> = _currentUserData

    init {
        checkUser()
    }

    private fun checkUser() {
        val firebaseUser = repository.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                val data = repository.getUserData(firebaseUser.uid)
                _currentUserData.value = data
                _authState.value = AuthState.Authenticated
            }
        }
    }

    fun login(username: String, pass: String) {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Lütfen tüm alanları doldurun.")
            return
        }
        
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // 1. Normal girişi dene
                repository.login(cleanUsername, pass)
                completeLoginFlow()
            } catch (e: Exception) {
                Log.d("UniFlowAuth", "Login failed, checking Firestore for imported user: $cleanUsername")
                
                // 2. Eğer normal giriş başarısızsa, Excel'den gelen hoca mı diye bak
                try {
                    val importedUser = repository.findImportedUser(cleanUsername)
                    if (importedUser != null && importedUser.password == pass) {
                        // Şifre doğru! Kullanıcıyı Firebase Auth'a "yükselt"
                        repository.upgradeImportedUser(importedUser, pass)
                        completeLoginFlow()
                    } else {
                        // Kullanıcı hiç yok veya şifre yanlış
                        _authState.value = AuthState.Error(if (importedUser != null) "Hatalı şifre." else "Kullanıcı bulunamadı.")
                    }
                } catch (innerE: Exception) {
                    _authState.value = AuthState.Error(mapFirebaseError(innerE))
                }
            }
        }
    }

    private suspend fun completeLoginFlow() {
        val firebaseUser = repository.currentUser
        if (firebaseUser != null) {
            val data = repository.getUserData(firebaseUser.uid)
            _currentUserData.value = data
            _authState.value = AuthState.Authenticated
        }
    }

    fun register(user: User, pass: String) {
        if (!PasswordValidator.isValid(pass)) {
            _authState.value = AuthState.Error(PasswordValidator.getErrorMessage())
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.register(user, pass)
                completeLoginFlow()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapFirebaseError(e))
            }
        }
    }

    fun completeOnboarding() {
        val uid = repository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.setOnboarded(uid)
                _currentUserData.value = _currentUserData.value?.copy(onboarded = true)
            } catch (e: Exception) {
                Log.e("UniFlowAuth", "Onboarding save failed", e)
            }
        }
    }

    private fun mapFirebaseError(e: Exception): String {
        return if (e is FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_EMAIL", "ERROR_USER_NOT_FOUND" -> "Kullanıcı adı veya şifre hatalı."
                "ERROR_WRONG_PASSWORD" -> "Hatalı şifre."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Bu kullanıcı adı zaten alınmış."
                "ERROR_WEAK_PASSWORD" -> "Şifre çok zayıf."
                else -> "Hata: ${e.localizedMessage}"
            }
        } else {
            e.localizedMessage ?: "Beklenmedik bir hata oluştu."
        }
    }

    fun logout() {
        repository.logout()
        _currentUserData.value = null
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
