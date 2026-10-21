package com.example.tradenetwork.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditListingViewModel(
    private val productId: String,
    private val repository: ProductRepository = ProductRepositoryImpl()
) : ViewModel() {

    private var product: Product? = null

    var title = MutableStateFlow("")
    var description = MutableStateFlow("")
    var price = MutableStateFlow("")
    var category = MutableStateFlow("Electronics")
    var condition = MutableStateFlow("New")
    var location = MutableStateFlow("")
    var status = MutableStateFlow(ProductStatus.AVAILABLE)

    private val _existingImages = MutableStateFlow<List<String>>(emptyList())
    val existingImages: StateFlow<List<String>> = _existingImages.asStateFlow()

    private val _newImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val newImageUris: StateFlow<List<Uri>> = _newImageUris.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductById(productId)
                .onSuccess { prod ->
                    if (prod != null) {
                        product = prod
                        title.value = prod.title
                        description.value = prod.description
                        price.value = prod.price.toString()
                        category.value = prod.category
                        condition.value = prod.condition
                        location.value = prod.location
                        status.value = prod.status
                        _existingImages.value = prod.images
                    } else {
                        _errorMessage.value = "Product not found"
                    }
                }
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Failed to load product"
                }
            _isLoading.value = false
        }
    }

    fun removeExistingImage(url: String) {
        _existingImages.value = _existingImages.value.filter { it != url }
    }

    fun addNewImages(uris: List<Uri>) {
        val current = _newImageUris.value.toMutableList()
        val totalImages = _existingImages.value.size + current.size
        for (uri in uris) {
            if (totalImages + current.size < 10) {
                current.add(uri)
            }
        }
        _newImageUris.value = current
    }

    fun removeNewImage(uri: Uri) {
        _newImageUris.value = _newImageUris.value.filter { it != uri }
    }

    fun updateListing() {
        val currentProduct = product ?: return
        val t = title.value.trim()
        val d = description.value.trim()
        val pStr = price.value.trim()
        val cat = category.value
        val cond = condition.value
        val loc = location.value.trim()
        val stat = status.value
        val existing = _existingImages.value
        val newUris = _newImageUris.value

        if (t.isEmpty() || d.isEmpty() || loc.isEmpty()) {
            _errorMessage.value = "Fields cannot be empty"
            return
        }
        val priceVal = pStr.toDoubleOrNull()
        if (priceVal == null || priceVal <= 0.0) {
            _errorMessage.value = "Invalid price"
            return
        }
        if (existing.isEmpty() && newUris.isEmpty()) {
            _errorMessage.value = "At least one image is required"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val updatedProduct = currentProduct.copy(
                title = t,
                description = d,
                price = priceVal,
                category = cat,
                condition = cond,
                location = loc,
                images = existing,
                status = stat
            )

            repository.updateProduct(updatedProduct, newUris)
                .onSuccess {
                    _isSuccess.value = true
                }
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Failed to update listing"
                }
            _isLoading.value = false
        }
    }
}
