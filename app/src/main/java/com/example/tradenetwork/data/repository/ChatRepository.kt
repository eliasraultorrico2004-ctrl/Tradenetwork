package com.example.tradenetwork.data.repository

import com.example.tradenetwork.data.model.Conversation
import com.example.tradenetwork.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getOrCreateConversation(
        buyerId: String,
        sellerId: String,
        productId: String,
        productTitle: String,
        productImage: String
    ): Result<String>

    fun getConversations(userId: String): Flow<Result<List<Conversation>>>
    fun getMessages(conversationId: String): Flow<Result<List<Message>>>
    suspend fun sendMessage(conversationId: String, senderId: String, text: String, recipientId: String): Result<Unit>
    suspend fun markAsRead(conversationId: String, userId: String): Result<Unit>
}
