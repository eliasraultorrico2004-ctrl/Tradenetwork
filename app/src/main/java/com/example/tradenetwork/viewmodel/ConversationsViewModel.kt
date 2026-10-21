package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Conversation
import com.example.tradenetwork.data.repository.ChatRepository
import com.example.tradenetwork.data.repository.ChatRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ConversationsUiState {
    object Loading : ConversationsUiState()
    data class Success(val conversations: List<Conversation>, val currentUserId: String) : ConversationsUiState()
    object Empty : ConversationsUiState()
    data class Error(val message: String) : ConversationsUiState()
}

class ConversationsViewModel(
    private val chatRepository: ChatRepository = ChatRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConversationsUiState>(ConversationsUiState.Loading)
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = ConversationsUiState.Error("User not authenticated")
            return
        }

        chatRepository.getConversations(currentUser.uid)
            .onEach { result ->
                result.onSuccess { conversations ->
                    _uiState.value = if (conversations.isEmpty()) {
                        ConversationsUiState.Empty
                    } else {
                        ConversationsUiState.Success(conversations, currentUser.uid)
                    }
                }.onFailure { error ->
                    _uiState.value = ConversationsUiState.Error(error.localizedMessage ?: "Failed to load chats")
                }
            }
            .launchIn(viewModelScope)
    }
}
