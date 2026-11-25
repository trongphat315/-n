package com.example.doan.model

data class HistorySummary(
    val totalAttempts: Int,
    val accuracyPercent: Int,
    val bestStreak: Int
)

data class HistoryItem(
    val attemptId: String,
    val mode: String, // "PRACTICE" hoặc "PVP"
    val title: String,
    val timestamp: String,
    val score: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val durationSeconds: Int
)

data class HistoryResponse(
    val summary: HistorySummary,
    val items: List<HistoryItem>
)