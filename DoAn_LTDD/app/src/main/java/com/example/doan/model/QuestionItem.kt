package com.example.doan.model

data class QuestionItem(
    val id: Int,
    val content: String,
    val options: List<String>,
    val correctIndex: Int
)

data class QuizSessionResponse(
    val quizId: Int,
    val topicTitle: String,
    val timePerQuestionSeconds: Int = 15,
    val questions: List<QuestionItem>,
    val backupQuestion: QuestionItem? = null
)