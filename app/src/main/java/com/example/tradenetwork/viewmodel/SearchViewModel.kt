package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    NEWEST, OLDEST, PRICE_LOW_TO_HIGH, PRICE_HIGH_TO_LOW
}

class SearchViewModel(
    private val productRepository: ProductRepository = ProductRepositoryImpl()
) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val minPrice = MutableStateFlow("")
    val maxPrice = MutableStateFlow("")
    val selectedCondition = MutableStateFlow<String?>(null)
    val selectedLocation = MutableStateFlow<String?>(null)
    val sortOption = MutableStateFlow(SortOption.NEWEST)

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        fetchAllProducts()
        observeFilters()
    }

    private fun fetchAllProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            productRepository.getProducts(null)
                .onEach { result ->
                    result.onSuccess { products ->
                        _allProducts.value = products
                        applyFiltersAndSorting()
                    }.onFailure { error ->
                        _uiState.value = ProductUiState.Error(error.localizedMessage ?: "Failed to load products")
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun observeFilters() {
        listOf(
            searchQuery, selectedCategory, minPrice, maxPrice,
            selectedCondition, selectedLocation, sortOption
        ).forEach { flow ->
            flow.onEach { applyFiltersAndSorting() }
                .launchIn(viewModelScope)
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun setCategory(category: String) {
        selectedCategory.value = category
    }

    fun setPriceRange(min: String, max: String) {
        minPrice.value = min
        maxPrice.value = max
    }

    fun setCondition(condition: String?) {
        selectedCondition.value = condition
    }

    fun setLocation(location: String?) {
        selectedLocation.value = location
    }

    fun setSortOption(option: SortOption) {
        sortOption.value = option
    }

    fun clearAllFilters() {
        searchQuery.value = ""
        selectedCategory.value = "All"
        minPrice.value = ""
        maxPrice.value = ""
        selectedCondition.value = null
        selectedLocation.value = null
        sortOption.value = SortOption.NEWEST
    }

    private fun applyFiltersAndSorting() {
        val query = searchQuery.value.trim().lowercase()
        val cat = selectedCategory.value
        val minP = minPrice.value.toDoubleOrNull() ?: 0.0
        val maxP = maxPrice.value.toDoubleOrNull() ?: Double.MAX_VALUE
        val cond = selectedCondition.value
        val loc = selectedLocation.value?.trim()?.lowercase()
        val sort = sortOption.value

        var filtered = _allProducts.value.filter { product ->
            val matchesQuery = query.isEmpty() ||
                    product.title.lowercase().contains(query) ||
                    product.description.lowercase().contains(query)

            val matchesCategory = cat == "All" || product.category.equals(cat, ignoreCase = true)

            val matchesPrice = product.price in minP..maxP

            val matchesCondition = cond == null || product.condition.equals(cond, ignoreCase = true)

            val matchesLocation = loc.isNullOrEmpty() || product.location.lowercase().contains(loc)

            matchesQuery && matchesCategory && matchesPrice && matchesCondition && matchesLocation
        }

        filtered = when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt?.seconds ?: 0L }
            SortOption.OLDEST -> filtered.sortedBy { it.createdAt?.seconds ?: 0L }
            SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.price }
        }

        _uiState.value = if (filtered.isEmpty()) {
            ProductUiState.Empty
        } else {
            ProductUiState.Success(filtered)
        }
    }

    fun activeFiltersCount(): Int {
        var count = 0
        if (selectedCategory.value != "All") count++
        if (minPrice.value.isNotEmpty() || maxPrice.value.isNotEmpty()) count++
        if (selectedCondition.value != null) count++
        if (!selectedLocation.value.isNullOrEmpty()) count++
        return count
    }
}
