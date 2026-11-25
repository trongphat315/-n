package com.example.doan.model

enum class DifficultyLevel(val label: String) {
    EASY("Dễ"),
    MEDIUM("Trung bình"),
    HARD("Khó")
}
enum class CategoryIcon {
    TECH, HISTORY, SPACE, ENGLISH, GEOGRAPHY, LITERATURE
}

data class QuizSetItem(
    val id: Int,
    val title: String,
    val questionCount: Int,
    val difficulty: DifficultyLevel,
    val xpReward: Int
)

data class TopicItem(
    val id: Int,
    val title: String,
    val category: String,
    val totalQuestions: Int,
    val icon: CategoryIcon,
    val quizSets: List<QuizSetItem>
)