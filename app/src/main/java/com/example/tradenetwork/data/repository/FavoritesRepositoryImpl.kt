package com.example.tradenetwork.data.repository

import com.example.tradenetwork.data.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavoritesRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FavoritesRepository {

    override suspend fun addFavorite(userId: String, productId: String): Result<Unit> = try {
        val favoriteMap = hashMapOf(
            "productId" to productId,
            "createdAt" to Timestamp.now()
        )
        firestore.collection("users").document(userId)
            .collection("favorites").document(productId)
            .set(favoriteMap).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeFavorite(userId: String, productId: String): Result<Unit> = try {
        firestore.collection("users").document(userId)
            .collection("favorites").document(productId)
            .delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observeFavoriteIds(userId: String): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.map { it.id } ?: emptyList()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    override fun getFavoriteProducts(userId: String): Flow<Result<List<Product>>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("favorites")
            .addSnapshotListener { favSnapshot, favError ->
                if (favError != null) {
                    trySend(Result.failure(favError))
                    return@addSnapshotListener
                }

                val productIds = favSnapshot?.documents?.map { it.id } ?: emptyList()
                if (productIds.isEmpty()) {
                    trySend(Result.success(emptyList()))
                    return@addSnapshotListener
                }

                val chunks = productIds.chunked(30)
                val products = mutableListOf<Product>()
                var completed = 0

                for (chunk in chunks) {
                    firestore.collection("products")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .addOnSuccessListener { prodSnapshot ->
                            val chunkProducts = prodSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Product::class.java)?.copy(id = doc.id)
                            }
                            products.addAll(chunkProducts)
                            completed++
                            if (completed == chunks.size) {
                                trySend(Result.success(products))
                            }
                        }
                        .addOnFailureListener { e ->
                            trySend(Result.failure(e))
                        }
                }
            }

        awaitClose { listener.remove() }
    }
}
