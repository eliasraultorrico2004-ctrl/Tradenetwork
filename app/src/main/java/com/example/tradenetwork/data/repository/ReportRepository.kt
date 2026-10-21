package com.example.tradenetwork.data.repository

import com.example.tradenetwork.data.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun submitReport(report: Report): Result<Unit>
    fun getMyReports(userId: String): Flow<Result<List<Report>>>
}
