package com.example.tradenetwork.data.repository

import android.net.Uri
import com.example.tradenetwork.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null && snapshot.exists()) {
                            trySend(snapshot.toObject(User::class.java))
                        } else {
                            trySend(User(uid = firebaseUser.uid, name = firebaseUser.displayName ?: "", email = firebaseUser.email ?: ""))
                        }
                    }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("User creation failed")
        
        val user = User(
            uid = firebaseUser.uid,
            name = name,
            email = email,
            createdAt = Timestamp.now()
        )
        
        firestore.collection("users").document(firebaseUser.uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserById(userId: String): Result<User?> = try {
        val doc = firestore.collection("users").document(userId).get().await()
        val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProfile(userId: String, name: String, location: String, profileImage: String?): Result<Unit> = try {
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "location" to location,
            "updatedAt" to Timestamp.now()
        )
        if (profileImage != null) {
            updates["profileImage"] = profileImage
        }
        firestore.collection("users").document(userId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String> = try {
        val imageRef = FirebaseStorage.getInstance().reference.child("users/$userId/profile/profile.jpg")
        imageRef.putFile(uri).await()
        val downloadUrl = imageRef.downloadUrl.await().toString()
        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAccount(): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("No authenticated user")
        val uid = user.uid
        firestore.collection("users").document(uid).delete().await()
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
