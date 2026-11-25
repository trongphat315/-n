package com.example.doan.repository

import com.example.doan.api.RetrofitClient
import com.example.doan.api.TopicApi
import com.example.doan.model.HistoryItem
import com.example.doan.model.HistoryResponse
import com.example.doan.model.HistorySummary
import kotlinx.coroutines.delay

class HistoryRepository(
    private val topicApi: TopicApi = RetrofitClient.topicApi
) {
    suspend fun getUserHistory(mode: String? = null): Result<HistoryResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(220L)
            val newlySubmitted = QuizRepository.getSubmittedMockHistory()

            val defaultItems = listOf(
                HistoryItem(
                    attemptId = "att_01",
                    mode = "PRACTICE",
                    title = "Công nghệ & AI – Bộ đề #2",
                    timestamp = "12/11/2026 • 13:45",
                    score = 850,
                    correctCount = 8,
                    totalQuestions = 10,
                    durationSeconds = 102
                ),
                HistoryItem(
                    attemptId = "att_02",
                    mode = "PVP",
                    title = "Đấu 1v1 vs Hoàng Nam (Thắng)",
                    timestamp = "12/11/2026 • 11:20",
                    score = 920,
                    correctCount = 5,
                    totalQuestions = 5,
                    durationSeconds = 38
                ),
                HistoryItem(
                    attemptId = "att_03",
                    mode = "PRACTICE",
                    title = "Lịch sử Việt Nam – Bộ đề #1",
                    timestamp = "11/11/2026 • 20:15",
                    score = 900,
                    correctCount = 9,
                    totalQuestions = 10,
                    durationSeconds = 88
                ),
                HistoryItem(
                    attemptId = "att_04",
                    mode = "PVP",
                    title = "Đấu 1v1 vs Linh Chi (Thắng)",
                    timestamp = "10/11/2026 • 19:05",
                    score = 880,
                    correctCount = 4,
                    totalQuestions = 5,
                    durationSeconds = 44
                )
            )

            val combinedItems = newlySubmitted + defaultItems
            val filteredItems = if (mode.isNullOrBlank()) {
                combinedItems
            } else {
                combinedItems.filter { it.mode == mode }
            }

            return Result.success(
                HistoryResponse(
                    summary = HistorySummary(
                        totalAttempts = 42 + newlySubmitted.size,
                        accuracyPercent = 82,
                        bestStreak = 9
                    ),
                    items = filteredItems
                )
            )
        }

        return try {
            val response = topicApi.fetchUserHistory(
                userId = RetrofitClient.currentUserId,
                mode = mode
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không tải được lịch sử làm bài (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối máy chủ: ${e.localizedMessage}"))
        }
    }
}