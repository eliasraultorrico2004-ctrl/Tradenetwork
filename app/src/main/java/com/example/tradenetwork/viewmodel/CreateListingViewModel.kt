package com.example.tradenetwork.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.data.repository.ProductRepository
import com.example.tradenetwork.data.repository.ProductRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateListingViewModel(
    private val repository: ProductRepository = ProductRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var title = MutableStateFlow("")
    var description = MutableStateFlow("")
    var price = MutableStateFlow("")
    var category = MutableStateFlow("Electronics")
    var condition = MutableStateFlow("New")
    var location = MutableStateFlow("Santa Cruz")
    
    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun addImages(uris: List<Uri>) {
        val current = _imageUris.value.toMutableList()
        for (uri in uris) {
            if (current.size < 10) {
                current.add(uri)
            }
        }
        _imageUris.value = current
    }

    fun removeImage(uri: Uri) {
        _imageUris.value = _imageUris.value.filter { it != uri }
    }

    fun publishListing() {
        val t = title.value.trim()
        val d = description.value.trim()
        val pStr = price.value.trim()
        val cat = category.value
        val cond = condition.value
        val loc = location.value.trim()
        val images = _imageUris.value

        if (t.isEmpty()) {
            _errorMessage.value = "Title cannot be empty"
            return
        }
        if (d.isEmpty()) {
            _errorMessage.value = "Description cannot be empty"
            return
        }
        val priceVal = pStr.toDoubleOrNull()
        if (priceVal == null || priceVal <= 0.0) {
            _errorMessage.value = "Please enter a valid price greater than zero"
            return
        }
        if (loc.isEmpty()) {
            _errorMessage.value = "Location cannot be empty"
            return
        }
        if (images.isEmpty()) {
            _errorMessage.value = "Please add at least one product image"
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val product = Product(
                sellerId = currentUser.uid,
                title = t,
                description = d,
                price = priceVal,
                category = cat,
                condition = cond,
                location = loc,
                status = ProductStatus.AVAILABLE
            )

            repository.createProduct(product, images)
                .onSuccess {
                    _isSuccess.value = true
                }
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Failed to publish listing"
                }

            _isLoading.value = false
        }
    }
}
