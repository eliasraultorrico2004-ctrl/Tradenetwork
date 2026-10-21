package com.example.tradenetwork.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String? = null,
    val location: String? = null,
    val createdAt: Timestamp? = null
)
