package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Conversation
import com.example.tradenetwork.data.model.Message
import com.example.tradenetwork.data.model.User
import com.example.tradenetwork.data.repository.AuthRepository
import com.example.tradenetwork.data.repository.AuthRepositoryImpl
import com.example.tradenetwork.data.repository.ChatRepository
import com.example.tradenetwork.data.repository.ChatRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(
        val conversation: Conversation?,
        val messages: List<Message>,
        val otherUser: User?,
        val currentUserId: String
    ) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(
    private val conversationId: String,
    private val chatRepository: ChatRepository = ChatRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversation: Conversation? = null
    private var otherUser: User? = null
    private val currentUserId = auth.currentUser?.uid ?: ""

    init {
        loadChatData()
    }

    private fun loadChatData() {
        if (currentUserId.isEmpty()) {
            _uiState.value = ChatUiState.Error("User not authenticated")
            return
        }

        // Mark as read when opening chat
        viewModelScope.launch {
            chatRepository.markAsRead(conversationId, currentUserId)
        }

        // Observe messages
        chatRepository.getMessages(conversationId)
            .onEach { result ->
                result.onSuccess { messages ->
                    // Also get conversation details if needed or fetch once
                    // For simplicity, let's observe conversation or fetch it
                    val conversation = currentConversation
                    _uiState.value = ChatUiState.Success(
                        conversation = conversation,
                        messages = messages,
                        otherUser = otherUser,
                        currentUserId = currentUserId
                    )
                }.onFailure { error ->
                    _uiState.value = ChatUiState.Error(error.localizedMessage ?: "Failed to load messages")
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val currentUser = auth.currentUser ?: return
        val conv = currentConversation ?: return
        val recipientId = if (conv.buyerId == currentUser.uid) conv.sellerId else conv.buyerId

        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, currentUser.uid, trimmed, recipientId)
                .onFailure { e ->
                    // Handle error if needed
                }
        }
    }

    fun setConversation(conversation: Conversation) {
        currentConversation = conversation
        val otherUserId = if (conversation.buyerId == currentUserId) conversation.sellerId else conversation.buyerId
        viewModelScope.launch {
            authRepository.getUserById(otherUserId)
                .onSuccess { user ->
                    otherUser = user
                    val currentState = _uiState.value
                    if (currentState is ChatUiState.Success) {
                        _uiState.value = currentState.copy(conversation = conversation, otherUser = user)
                    }
                }
        }
    }
}
