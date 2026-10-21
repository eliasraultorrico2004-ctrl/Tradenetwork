package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.repository.FavoritesRepository
import com.example.tradenetwork.data.repository.FavoritesRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val products: List<Product>) : FavoritesUiState()
    object Empty : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository = FavoritesRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = FavoritesUiState.Error("User not authenticated")
            return
        }

        favoritesRepository.getFavoriteProducts(currentUser.uid)
            .onEach { result ->
                result.onSuccess { products ->
                    _uiState.value = if (products.isEmpty()) {
                        FavoritesUiState.Empty
                    } else {
                        FavoritesUiState.Success(products)
                    }
                }.onFailure { error ->
                    _uiState.value = FavoritesUiState.Error(error.localizedMessage ?: "Failed to load favorites")
                }
            }
            .launchIn(viewModelScope)
    }

    fun removeFavorite(productId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            favoritesRepository.removeFavorite(currentUser.uid, productId)
        }
    }
}
