package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.data.model.User
import com.example.tradenetwork.data.repository.AuthRepository
import com.example.tradenetwork.data.repository.AuthRepositoryImpl
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: User?,
        val activeListingsCount: Int,
        val soldListingsCount: Int
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val productRepository: ProductRepository = ProductRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = ProfileUiState.Error("User not authenticated")
            return
        }

        combine(
            authRepository.currentUser,
            productRepository.getMyListings(currentUser.uid)
        ) { user, productResult ->
            try {
                val products = productResult.getOrDefault(emptyList())
                val activeCount = products.count { it.status == ProductStatus.AVAILABLE }
                val soldCount = products.count { it.status == ProductStatus.SOLD }
                ProfileUiState.Success(user, activeCount, soldCount)
            } catch (e: Exception) {
                ProfileUiState.Error(e.localizedMessage ?: "Failed to load profile")
            }
        }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }
}
