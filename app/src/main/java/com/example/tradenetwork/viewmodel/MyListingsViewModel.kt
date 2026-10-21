package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class MyListingsUiState {
    object Loading : MyListingsUiState()
    data class Success(val products: List<Product>) : MyListingsUiState()
    data class Error(val message: String) : MyListingsUiState()
    object Empty : MyListingsUiState()
}

class MyListingsViewModel(
    private val repository: ProductRepository = ProductRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyListingsUiState>(MyListingsUiState.Loading)
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    init {
        loadMyListings()
    }

    fun loadMyListings() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = MyListingsUiState.Error("User not authenticated")
            return
        }

        repository.getMyListings(currentUser.uid)
            .onEach { result ->
                result.onSuccess { products ->
                    _uiState.value = if (products.isEmpty()) {
                        MyListingsUiState.Empty
                    } else {
                        MyListingsUiState.Success(products)
                    }
                }.onFailure { error ->
                    _uiState.value = MyListingsUiState.Error(error.localizedMessage ?: "Failed to load listings")
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateStatus(productId: String, status: ProductStatus) {
        viewModelScope.launch {
            repository.updateProductStatus(productId, status)
                .onFailure { e ->
                    // Handle failure if needed
                }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
                .onFailure { e ->
                    // Handle failure if needed
                }
        }
    }
}
