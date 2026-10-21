package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.User
import com.example.tradenetwork.data.repository.AuthRepository
import com.example.tradenetwork.data.repository.AuthRepositoryImpl
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SellerProfileUiState {
    object Loading : SellerProfileUiState()
    data class Success(val seller: User?, val products: List<Product>) : SellerProfileUiState()
    data class Error(val message: String) : SellerProfileUiState()
}

class SellerProfileViewModel(
    private val sellerId: String,
    private val repository: ProductRepository = ProductRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SellerProfileUiState>(SellerProfileUiState.Loading)
    val uiState: StateFlow<SellerProfileUiState> = _uiState.asStateFlow()

    init {
        loadSellerProfile()
    }

    private fun loadSellerProfile() {
        viewModelScope.launch {
            val sellerResult = authRepository.getUserById(sellerId)
            val seller = sellerResult.getOrNull()

            repository.getProductsBySellerId(sellerId)
                .onEach { result ->
                    result.onSuccess { products ->
                        _uiState.value = SellerProfileUiState.Success(seller, products)
                    }.onFailure { error ->
                        _uiState.value = SellerProfileUiState.Error(error.localizedMessage ?: "Failed to load seller profile")
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}
