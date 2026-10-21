package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.repository.AuthRepository
import com.example.tradenetwork.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AccountSettingsUiState {
    object Idle : AccountSettingsUiState()
    object Loading : AccountSettingsUiState()
    data class Success(val message: String) : AccountSettingsUiState()
    data class Error(val message: String) : AccountSettingsUiState()
}

class AccountSettingsViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountSettingsUiState>(AccountSettingsUiState.Idle)
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    fun sendPasswordResetEmail() {
        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.email.isNullOrEmpty()) {
            _uiState.value = AccountSettingsUiState.Error("No user email found")
            return
        }

        viewModelScope.launch {
            _uiState.value = AccountSettingsUiState.Loading
            authRepository.resetPassword(currentUser.email!!)
                .onSuccess {
                    _uiState.value = AccountSettingsUiState.Success("Password reset email sent. Check your inbox.")
                }
                .onFailure { e ->
                    _uiState.value = AccountSettingsUiState.Error(e.localizedMessage ?: "Failed to send reset email")
                }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = AccountSettingsUiState.Loading
            authRepository.deleteAccount()
                .onSuccess {
                    _uiState.value = AccountSettingsUiState.Success("Account deleted successfully")
                }
                .onFailure { e ->
                    _uiState.value = AccountSettingsUiState.Error(e.localizedMessage ?: "Failed to delete account. You may need to re-authenticate.")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
