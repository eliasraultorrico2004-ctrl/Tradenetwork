package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.data.model.User
import com.example.tradenetwork.data.repository.AuthRepository
import com.example.tradenetwork.data.repository.AuthRepositoryImpl
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ProductDetailsUiState {
    object Loading : ProductDetailsUiState()
    data class Success(
        val product: Product,
        val seller: User?,
        val isOwner: Boolean,
        val isFavorite: Boolean
    ) : ProductDetailsUiState()
    object NotFound : ProductDetailsUiState()
    data class Error(val message: String) : ProductDetailsUiState()
}

class ProductDetailsViewModel(
    private val productId: String,
    private val repository: ProductRepository = ProductRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        observeProduct()
    }

    private fun observeProduct() {
        repository.observeProductById(productId)
            .onEach { result ->
                result.onSuccess { product ->
                    if (product == null) {
                        _uiState.value = ProductDetailsUiState.NotFound
                    } else {
                        val currentUserId = auth.currentUser?.uid
                        val isOwner = currentUserId != null && product.sellerId == currentUserId

                        // Fetch seller info
                        authRepository.getUserById(product.sellerId)
                            .onSuccess { seller ->
                                _uiState.value = ProductDetailsUiState.Success(
                                    product = product,
                                    seller = seller,
                                    isOwner = isOwner,
                                    isFavorite = _isFavorite.value
                                )
                            }
                            .onFailure {
                                _uiState.value = ProductDetailsUiState.Success(
                                    product = product,
                                    seller = null,
                                    isOwner = isOwner,
                                    isFavorite = _isFavorite.value
                                )
                            }
                    }
                }.onFailure { error ->
                    _uiState.value = ProductDetailsUiState.Error(error.localizedMessage ?: "Failed to load product")
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
        val currentState = _uiState.value
        if (currentState is ProductDetailsUiState.Success) {
            _uiState.value = currentState.copy(isFavorite = _isFavorite.value)
        }
    }

    fun updateStatus(status: ProductStatus) {
        viewModelScope.launch {
            repository.updateProductStatus(productId, status)
        }
    }

    fun deleteProduct() {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun reportProduct(reason: String) {
        // Prepare for future moderation reporting phase
        viewModelScope.launch {
            // Implementation for logging/saving report
        }
    }
}
