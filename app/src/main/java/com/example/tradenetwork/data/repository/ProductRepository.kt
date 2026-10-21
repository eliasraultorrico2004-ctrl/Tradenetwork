package com.example.tradenetwork.data.repository

import android.net.Uri
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(category: String? = null): Flow<Result<List<Product>>>
    suspend fun refreshProducts(): Result<List<Product>>
    fun getMyListings(sellerId: String): Flow<Result<List<Product>>>
    suspend fun getProductById(productId: String): Result<Product?>
    fun observeProductById(productId: String): Flow<Result<Product?>>
    fun getProductsBySellerId(sellerId: String): Flow<Result<List<Product>>>
    suspend fun createProduct(product: Product, imageUris: List<Uri>): Result<String>
    suspend fun updateProduct(product: Product, newImageUris: List<Uri>): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
    suspend fun updateProductStatus(productId: String, status: ProductStatus): Result<Unit>
}
