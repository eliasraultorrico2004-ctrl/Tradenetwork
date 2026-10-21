package com.example.tradenetwork.data.repository

import android.net.Uri
import com.example.tradenetwork.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun updateProfile(userId: String, name: String, location: String, profileImage: String?): Result<Unit>
    suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String>
    suspend fun deleteAccount(): Result<Unit>
}
