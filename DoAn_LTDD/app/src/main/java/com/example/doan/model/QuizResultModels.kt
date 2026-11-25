package com.example.doan.model

data class QuizResultResponse(
    val attemptId: String,
    val quizId: Int,
    val quizTitle: String,
    val score: Int,
    val earnedXp: Int,
    val earnedCoins: Int = 0,
    val accuracyPercent: Int,
    val performanceTitle: String,
    val performanceSubtitle: String,
    val durationSeconds: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val totalQuestions: Int
)