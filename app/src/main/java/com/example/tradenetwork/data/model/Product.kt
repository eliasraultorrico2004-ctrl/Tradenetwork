package com.example.tradenetwork.data.model

import com.google.firebase.Timestamp

enum class ProductStatus {
    AVAILABLE,
    RESERVED,
    SOLD
}

data class Product(
    val id: String = "",
    val sellerId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val condition: String = "",
    val location: String = "",
    val images: List<String> = emptyList(),
    val status: ProductStatus = ProductStatus.AVAILABLE,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
