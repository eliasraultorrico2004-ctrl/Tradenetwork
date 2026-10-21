package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.repository.AuthRepository
import com.example.tradenetwork.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String? = null) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.login(email, password)
                .onSuccess {
                    _uiState.value = AuthUiState.Success()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapFirebaseError(error))
                }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.register(name, email, password)
                .onSuccess {
                    _uiState.value = AuthUiState.Success()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapFirebaseError(error))
                }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.resetPassword(email)
                .onSuccess {
                    _uiState.value = AuthUiState.Success("Reset email sent")
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapFirebaseError(error))
                }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    private fun mapFirebaseError(error: Throwable): String {
        return when (error.message) {
            "The email address is already in use by another account." -> "This email is already registered."
            "The email address is badly formatted." -> "Invalid email format."
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "User not found."
            "The password is invalid or the user does not have a password." -> "Incorrect password."
            else -> error.localizedMessage ?: "An unexpected error occurred."
        }
    }
}
