package com.example.tradenetwork.data.repository

import com.example.tradenetwork.data.model.Report
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReportRepository {

    override suspend fun submitReport(report: Report): Result<Unit> = try {
        val reportId = firestore.collection("reports").document().id
        val now = Timestamp.now()
        val reportMap = hashMapOf(
            "id" to reportId,
            "reporterId" to report.reporterId,
            "targetType" to report.targetType,
            "targetId" to report.targetId,
            "reason" to report.reason,
            "description" to report.description,
            "status" to report.status,
            "createdAt" to now,
            "reviewerId" to null,
            "reviewedAt" to null
        )
        firestore.collection("reports").document(reportId).set(reportMap).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getMyReports(userId: String): Flow<Result<List<Report>>> = callbackFlow {
        val query = firestore.collection("reports")
            .whereEqualTo("reporterId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val reports = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Report::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Result.success(reports))
        }
        awaitClose { listener.remove() }
    }
}
