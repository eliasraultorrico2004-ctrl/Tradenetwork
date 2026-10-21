package com.example.tradenetwork.data.model

import com.google.firebase.Timestamp

data class Conversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val buyerId: String = "",
    val sellerId: String = "",
    val productId: String = "",
    val productTitle: String = "",
    val productImage: String = "",
    val lastMessage: String = "",
    val lastMessageAt: Timestamp? = null,
    val lastMessageSenderId: String = "",
    val unreadCounts: Map<String, Long> = emptyMap(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
