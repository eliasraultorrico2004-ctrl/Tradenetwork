package com.example.tradenetwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradenetwork.data.model.Report
import com.example.tradenetwork.data.repository.ReportRepository
import com.example.tradenetwork.data.repository.ReportRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    object Success : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

class ReportViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun submitReport(targetType: String, targetId: String, reason: String, description: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = ReportUiState.Error("User not authenticated")
            return
        }

        if (reason.isBlank()) {
            _uiState.value = ReportUiState.Error("Please select a report reason")
            return
        }

        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            val report = Report(
                reporterId = currentUser.uid,
                targetType = targetType,
                targetId = targetId,
                reason = reason,
                description = description.trim()
            )

            reportRepository.submitReport(report)
                .onSuccess {
                    _uiState.value = ReportUiState.Success
                }
                .onFailure { e ->
                    _uiState.value = ReportUiState.Error(e.localizedMessage ?: "Failed to submit report")
                }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState.Idle
    }
}
