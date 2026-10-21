package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Report
import com.example.tradenetwork.data.repository.ReportRepository
import com.example.tradenetwork.data.repository.ReportRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class MyReportsUiState {
    object Loading : MyReportsUiState()
    data class Success(val reports: List<Report>) : MyReportsUiState()
    object Empty : MyReportsUiState()
    data class Error(val message: String) : MyReportsUiState()
}

class MyReportsViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyReportsUiState>(MyReportsUiState.Loading)
    val uiState: StateFlow<MyReportsUiState> = _uiState.asStateFlow()

    init {
        loadMyReports()
    }

    private fun loadMyReports() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = MyReportsUiState.Error("User not authenticated")
            return
        }

        reportRepository.getMyReports(currentUser.uid)
            .onEach { result ->
                result.onSuccess { reports ->
                    _uiState.value = if (reports.isEmpty()) {
                        MyReportsUiState.Empty
                    } else {
                        MyReportsUiState.Success(reports)
                    }
                }.onFailure { error ->
                    _uiState.value = MyReportsUiState.Error(error.localizedMessage ?: "Failed to load reports")
                }
            }
            .launchIn(viewModelScope)
    }
}
