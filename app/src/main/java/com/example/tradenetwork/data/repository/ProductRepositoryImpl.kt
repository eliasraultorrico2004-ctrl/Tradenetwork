package com.example.tradenetwork.data.repository

import android.net.Uri
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProductRepository {

    override fun getProducts(category: String?): Flow<Result<List<Product>>> = callbackFlow {
        var query = firestore.collection("products")
            .whereEqualTo("status", ProductStatus.AVAILABLE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        if (category != null && category != "All") {
            query = query.whereEqualTo("category", category)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            val products = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(Result.success(products))
        }

        awaitClose { listener.remove() }
    }

    override suspend fun refreshProducts(): Result<List<Product>> = try {
        val snapshot = firestore.collection("products")
            .whereEqualTo("status", ProductStatus.AVAILABLE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        val products = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Product::class.java)?.copy(id = doc.id)
        }
        Result.success(products)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getMyListings(sellerId: String): Flow<Result<List<Product>>> = callbackFlow {
        val query = firestore.collection("products")
            .whereEqualTo("sellerId", sellerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            val products = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(Result.success(products))
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getProductById(productId: String): Result<Product?> = try {
        val doc = firestore.collection("products").document(productId).get().await()
        val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
        Result.success(product)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observeProductById(productId: String): Flow<Result<Product?>> = callbackFlow {
        val listener = firestore.collection("products").document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(Result.success(null))
                    return@addSnapshotListener
                }
                val product = snapshot.toObject(Product::class.java)?.copy(id = snapshot.id)
                trySend(Result.success(product))
            }
        awaitClose { listener.remove() }
    }

    override fun getProductsBySellerId(sellerId: String): Flow<Result<List<Product>>> = callbackFlow {
        val query = firestore.collection("products")
            .whereEqualTo("sellerId", sellerId)
            .whereEqualTo("status", ProductStatus.AVAILABLE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val products = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Result.success(products))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createProduct(product: Product, imageUris: List<Uri>): Result<String> = try {
        val productId = firestore.collection("products").document().id
        val imageUrls = imageUris.map { it.toString() }

        val now = Timestamp.now()
        val productMap = hashMapOf(
            "id" to productId,
            "sellerId" to product.sellerId,
            "title" to product.title,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            "condition" to product.condition,
            "location" to product.location,
            "images" to imageUrls,
            "status" to product.status.name,
            "createdAt" to now,
            "updatedAt" to now
        )

        firestore.collection("products").document(productId).set(productMap).await()
        Result.success(productId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProduct(product: Product, newImageUris: List<Uri>): Result<Unit> = try {
        val productId = product.id
        val updatedImageUrls = product.images.toMutableList()
        for (uri in newImageUris) {
            updatedImageUrls.add(uri.toString())
        }

        val updates = hashMapOf<String, Any>(
            "title" to product.title,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            "condition" to product.condition,
            "location" to product.location,
            "images" to updatedImageUrls,
            "status" to product.status.name,
            "updatedAt" to Timestamp.now()
        )

        firestore.collection("products").document(productId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = try {
        firestore.collection("products").document(productId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProductStatus(productId: String, status: ProductStatus): Result<Unit> = try {
        val updates = hashMapOf<String, Any>(
            "status" to status.name,
            "updatedAt" to Timestamp.now()
        )
        firestore.collection("products").document(productId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
