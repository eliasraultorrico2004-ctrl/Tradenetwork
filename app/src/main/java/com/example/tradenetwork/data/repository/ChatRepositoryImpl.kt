package com.example.tradenetwork.data.repository

import com.example.tradenetwork.data.model.Conversation
import com.example.tradenetwork.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatRepository {

    override suspend fun getOrCreateConversation(
        buyerId: String,
        sellerId: String,
        productId: String,
        productTitle: String,
        productImage: String
    ): Result<String> = try {
        val conversationId = "conv_${buyerId}_${sellerId}_${productId}"
        val docRef = firestore.collection("conversations").document(conversationId)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            val now = Timestamp.now()
            val conversation = hashMapOf(
                "id" to conversationId,
                "participantIds" to listOf(buyerId, sellerId),
                "buyerId" to buyerId,
                "sellerId" to sellerId,
                "productId" to productId,
                "productTitle" to productTitle,
                "productImage" to productImage,
                "lastMessage" to "",
                "lastMessageAt" to now,
                "lastMessageSenderId" to "",
                "unreadCounts" to mapOf(buyerId to 0L, sellerId to 0L),
                "createdAt" to now,
                "updatedAt" to now
            )
            docRef.set(conversation).await()
        }
        Result.success(conversationId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getConversations(userId: String): Flow<Result<List<Conversation>>> = callbackFlow {
        val query = firestore.collection("conversations")
            .whereArrayContains("participantIds", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val conversations = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Conversation::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Result.success(conversations))
        }
        awaitClose { listener.remove() }
    }

    override fun getMessages(conversationId: String): Flow<Result<List<Message>>> = callbackFlow {
        val query = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Result.success(messages))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String,
        recipientId: String
    ): Result<Unit> = try {
        val now = Timestamp.now()
        val messageMap = hashMapOf(
            "senderId" to senderId,
            "text" to text,
            "createdAt" to now,
            "type" to "text"
        )

        val conversationRef = firestore.collection("conversations").document(conversationId)

        firestore.runBatch { batch ->
            val messageRef = conversationRef.collection("messages").document()
            batch.set(messageRef, messageMap)

            val updates = hashMapOf<String, Any>(
                "lastMessage" to text,
                "lastMessageAt" to now,
                "lastMessageSenderId" to senderId,
                "updatedAt" to now,
                "unreadCounts.$recipientId" to FieldValue.increment(1)
            )
            batch.update(conversationRef, updates)
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markAsRead(conversationId: String, userId: String): Result<Unit> = try {
        val conversationRef = firestore.collection("conversations").document(conversationId)
        val updates = hashMapOf<String, Any>(
            "unreadCounts.$userId" to 0L
        )
        conversationRef.update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
