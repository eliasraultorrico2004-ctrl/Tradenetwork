package com.example.tradenetwork.data.model

import com.google.firebase.Timestamp

enum class TargetType {
    PRODUCT,
    USER
}

enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED,
    DISMISSED
}

val REPORT_REASONS = listOf(
    "Fraud or suspected scam",
    "Prohibited or inappropriate item",
    "Incorrect or misleading description",
    "Duplicate listing",
    "Suspicious seller",
    "Harassment or inappropriate behavior",
    "Other"
)

data class Report(
    val id: String = "",
    val reporterId: String = "",
    val targetType: String = TargetType.PRODUCT.name,
    val targetId: String = "",
    val reason: String = "",
    val description: String = "",
    val status: String = ReportStatus.PENDING.name,
    val createdAt: Timestamp? = null,
    val reviewerId: String? = null,
    val reviewedAt: Timestamp? = null
)
