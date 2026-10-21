package com.example.tradenetwork.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.repository.AuthRepository
import com.example.tradenetwork.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var name = MutableStateFlow("")
    var location = MutableStateFlow("")
    
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            authRepository.getUserById(currentUser.uid)
                .onSuccess { user ->
                    if (user != null) {
                        name.value = user.name
                        location.value = user.location ?: ""
                        _profileImageUrl.value = user.profileImage
                    }
                }
        }
    }

    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }

    fun saveProfile() {
        val n = name.value.trim()
        val l = location.value.trim()
        val currentUser = auth.currentUser

        if (n.isEmpty()) {
            _errorMessage.value = "Name cannot be empty"
            return
        }
        if (currentUser == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            var imageUrl = _profileImageUrl.value
            val uri = _profileImageUri.value

            if (uri != null) {
                val uploadResult = authRepository.uploadProfileImage(currentUser.uid, uri)
                if (uploadResult.isSuccess) {
                    imageUrl = uploadResult.getOrNull()
                } else {
                    _errorMessage.value = "Failed to upload profile image"
                    _isLoading.value = false
                    return@launch
                }
            }

            authRepository.updateProfile(currentUser.uid, n, l, imageUrl)
                .onSuccess {
                    _isSuccess.value = true
                }
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Failed to update profile"
                }

            _isLoading.value = false
        }
    }
}
