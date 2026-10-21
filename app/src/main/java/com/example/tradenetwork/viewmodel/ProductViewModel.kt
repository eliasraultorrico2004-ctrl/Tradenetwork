package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
    object Empty : ProductUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModel(
    private val repository: ProductRepository = ProductRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeProducts()
    }

    private fun observeProducts() {
        _selectedCategory
            .flatMapLatest { category ->
                repository.getProducts(if (category == "All") null else category)
            }
            .onEach { result ->
                result.onSuccess { products ->
                    filterAndSearch(products)
                }.onFailure { error ->
                    _uiState.value = ProductUiState.Error(error.localizedMessage ?: "Failed to load products")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            refreshCurrentList()
        }
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshProducts()
                .onSuccess { products ->
                    filterAndSearch(products)
                }
                .onFailure { error ->
                    _uiState.value = ProductUiState.Error(error.localizedMessage ?: "Failed to refresh")
                }
            _isRefreshing.value = false
        }
    }

    private fun refreshCurrentList() {
        // This will re-trigger the flatMapLatest if we were to change something, 
        // but here we just want to re-filter the existing successful state if possible.
        // For simplicity, we just let the flow handle it or manually re-filter.
        viewModelScope.launch {
            val result = repository.refreshProducts()
            result.onSuccess { products ->
                filterAndSearch(products)
            }
        }
    }

    private fun filterAndSearch(products: List<Product>) {
        val query = _searchQuery.value.lowercase()
        val filtered = if (query.isEmpty()) {
            products
        } else {
            products.filter { it.title.lowercase().contains(query) }
        }

        _uiState.value = if (filtered.isEmpty()) {
            ProductUiState.Empty
        } else {
            ProductUiState.Success(filtered)
        }
    }
}
