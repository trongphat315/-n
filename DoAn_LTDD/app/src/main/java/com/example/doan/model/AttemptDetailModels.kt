package com.example.doan.model

data class QuestionReviewItem(
    val questionNumber: Int,
    val categoryLabel: String,
    val questionContent: String,
    val userAnswerText: String?, // null nếu hết giờ chưa chọn đáp án
    val correctAnswerText: String,
    val isCorrect: Boolean,
    val explanation: String
)

data class AttemptDetailResponse(
    val attemptId: String,
    val quizTitle: String,
    val score: Int,
    val maxScore: Int,
    val questions: List<QuestionReviewItem>
)