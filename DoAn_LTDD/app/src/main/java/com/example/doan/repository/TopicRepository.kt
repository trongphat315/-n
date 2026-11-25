package com.example.doan.repository

import com.example.doan.api.RetrofitClient
import com.example.doan.api.TopicApi
import com.example.doan.model.CategoryIcon
import com.example.doan.model.DifficultyLevel
import com.example.doan.model.QuizSetItem
import com.example.doan.model.TopicItem

class TopicRepository(
    private val topicApi: TopicApi = RetrofitClient.topicApi
) {
    fun getAllTopics(): List<TopicItem> = listOf(
        TopicItem(
            id = 1,
            title = "Công nghệ & AI",
            category = "Công nghệ",
            totalQuestions = 45,
            icon = CategoryIcon.TECH,
            quizSets = listOf(
                QuizSetItem(1, "Nhập môn AI & Machine Learning", 15, DifficultyLevel.EASY, 100),
                QuizSetItem(2, "Lập trình Android Jetpack Compose", 15, DifficultyLevel.MEDIUM, 150),
                QuizSetItem(3, "Kiến trúc hệ thống & Microservices", 15, DifficultyLevel.HARD, 200)
            )
        ),
        TopicItem(
            id = 2,
            title = "Lịch sử Việt Nam",
            category = "Lịch sử",
            totalQuestions = 30,
            icon = CategoryIcon.HISTORY,
            quizSets = listOf(
                QuizSetItem(4, "Các triều đại phong kiến Việt Nam", 10, DifficultyLevel.EASY, 80),
                QuizSetItem(5, "Chiến thắng Điện Biên Phủ 1954", 10, DifficultyLevel.MEDIUM, 120),
                QuizSetItem(6, "Đại thắng mùa Xuân 1975", 10, DifficultyLevel.HARD, 180)
            )
        ),
        TopicItem(
            id = 3,
            title = "Khoa học Vũ trụ",
            category = "Khoa học",
            totalQuestions = 35,
            icon = CategoryIcon.SPACE,
            quizSets = listOf(
                QuizSetItem(7, "Hệ Mặt Trời & Các hành tinh", 12, DifficultyLevel.EASY, 90),
                QuizSetItem(8, "Khám phá các Dải Ngân Hà", 13, DifficultyLevel.MEDIUM, 140),
                QuizSetItem(9, "Vật lý Thiên văn nâng cao", 10, DifficultyLevel.HARD, 210)
            )
        ),
        TopicItem(
            id = 4,
            title = "Tiếng Anh Giao tiếp",
            category = "Tiếng Anh",
            totalQuestions = 40,
            icon = CategoryIcon.ENGLISH,
            quizSets = listOf(
                QuizSetItem(10, "Từ vựng thông dụng hàng ngày", 15, DifficultyLevel.EASY, 70),
                QuizSetItem(11, "Ngữ pháp tiếng Anh nâng cao", 15, DifficultyLevel.MEDIUM, 130),
                QuizSetItem(12, "Luyện thi TOEIC / IELTS", 10, DifficultyLevel.HARD, 190)
            )
        )
    )

    suspend fun fetchAllTopics(): List<TopicItem> {
        return try {
            val response = topicApi.fetchAllTopics()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!!
            } else {
                getAllTopics()
            }
        } catch (e: Exception) {
            getAllTopics()
        }
    }
}
