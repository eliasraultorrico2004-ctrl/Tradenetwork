package com.example.tradenetwork.data.repository

import com.example.tradenetwork.data.model.Product
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addFavorite(userId: String, productId: String): Result<Unit>
    suspend fun removeFavorite(userId: String, productId: String): Result<Unit>
    fun observeFavoriteIds(userId: String): Flow<List<String>>
    fun getFavoriteProducts(userId: String): Flow<Result<List<Product>>>
}
