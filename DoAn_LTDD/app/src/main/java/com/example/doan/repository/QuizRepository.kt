package com.example.doan.repository

import com.example.doan.api.RetrofitClient
import com.example.doan.api.SubmitQuizRequest
import com.example.doan.api.SubmitQuizResponse
import com.example.doan.api.TopicApi
import com.example.doan.model.AttemptDetailResponse
import com.example.doan.model.HistoryItem
import com.example.doan.model.QuestionItem
import com.example.doan.model.QuestionReviewItem
import com.example.doan.model.QuizResultResponse
import com.example.doan.model.QuizSessionResponse
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizRepository(
    private val topicApi: TopicApi = RetrofitClient.topicApi
) {
    companion object {
        private val mockAttempts = mutableMapOf<String, QuizResultResponse>()
        private val mockAttemptDetails = mutableMapOf<String, AttemptDetailResponse>()
        private val mockHistoryItems = mutableListOf<HistoryItem>()

        fun getSubmittedMockHistory(): List<HistoryItem> = mockHistoryItems.toList()
    }

    suspend fun getQuizSession(quizId: Int): Result<QuizSessionResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(250L)
            return Result.success(
                QuizSessionResponse(
                    quizId = quizId,
                    topicTitle = "Công nghệ & AI – Bộ đề #$quizId",
                    timePerQuestionSeconds = 15,
                    questions = listOf(
                        QuestionItem(
                            id = 1,
                            content = "Ngôn ngữ lập trình nào được Google khuyến nghị chính thức để phát triển Android?",
                            options = listOf("Swift", "Kotlin", "Ruby", "PHP"),
                            correctIndex = 1
                        ),
                        QuestionItem(
                            id = 2,
                            content = "Thành phần nào trong kiến trúc MVVM giữ trạng thái giao diện khi xoay màn hình?",
                            options = listOf("Activity", "Retrofit", "ViewModel", "Interceptor"),
                            correctIndex = 2
                        ),
                        QuestionItem(
                            id = 3,
                            content = "Bộ công cụ giao diện khai báo (Declarative UI) hiện đại của Android tên là gì?",
                            options = listOf("Jetpack Compose", "Flutter SDK", "React Native", "SwiftUI"),
                            correctIndex = 0
                        ),
                        QuestionItem(
                            id = 4,
                            content = "Đơn vị xử lý trung tâm của máy tính được viết tắt là gì?",
                            options = listOf("RAM", "GPU", "CPU", "SSD"),
                            correctIndex = 2
                        ),
                        QuestionItem(
                            id = 5,
                            content = "Giao thức nào dùng để truyền tải dữ liệu mã hóa an toàn trên web?",
                            options = listOf("FTP", "HTTPS", "SMTP", "Telnet"),
                            correctIndex = 1
                        )
                    ),
                    backupQuestion = QuestionItem(
                        id = 99,
                        content = "Câu hỏi thay thế: Hệ điều hành Android được phát triển dựa trên nhân (kernel) nào?",
                        options = listOf("Windows NT", "Linux", "macOS Darwin", "FreeBSD"),
                        correctIndex = 1
                    )
                )
            )
        }

        return try {
            val response = topicApi.fetchQuizSession(quizId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không tải được bộ đề thi (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối máy chủ: ${e.localizedMessage}"))
        }
    }

    suspend fun submitResult(
        quizId: Int,
        quizTitle: String,
        request: SubmitQuizRequest,
        questionReviews: List<QuestionReviewItem> = emptyList(),
        mode: String = "PRACTICE"
    ): Result<SubmitQuizResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(200L)
            val attemptId = "attempt_${System.currentTimeMillis()}"
            val resolvedTitle = quizTitle.ifBlank { "Công nghệ & AI – Bộ đề #$quizId" }
            val earnedXp = if (request.correctCount == 0) 0 else (request.score / 6).coerceAtLeast(15)
            val total = request.totalQuestions.coerceAtLeast(1)
            val accuracy = (request.correctCount * 100) / total
            val wrong = (total - request.correctCount).coerceAtLeast(0)

            val (perfTitle, perfSub) = when {
                accuracy >= 80 -> Pair(
                    "Tốc độ & Chính xác Tuyệt vời!",
                    "Bạn nhanh hơn 84% người chơi cùng chủ đề."
                )
                accuracy >= 50 -> Pair(
                    "Phong độ Ổn định!",
                    "Bạn vượt qua 62% người chơi cùng chủ đề."
                )
                else -> Pair(
                    "Cần Luyện Tập Thêm!",
                    "Hãy thử lại bộ đề để cải thiện tốc độ và điểm số."
                )
            }

            mockAttempts[attemptId] = QuizResultResponse(
                attemptId = attemptId,
                quizId = quizId,
                quizTitle = resolvedTitle,
                score = request.score,
                earnedXp = earnedXp,
                accuracyPercent = accuracy,
                performanceTitle = perfTitle,
                performanceSubtitle = perfSub,
                durationSeconds = request.durationSeconds,
                correctCount = request.correctCount,
                wrongCount = wrong,
                totalQuestions = total
            )

            if (questionReviews.isNotEmpty()) {
                // Điểm tối đa mỗi câu = 10 + 15 * 15 = 235 điểm
                val maxPossibleScore = total * 235
                mockAttemptDetails[attemptId] = AttemptDetailResponse(
                    attemptId = attemptId,
                    quizTitle = resolvedTitle,
                    score = request.score,
                    maxScore = maxPossibleScore,
                    questions = questionReviews
                )
            }

            val currentTimestamp = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                .format(Date())

            mockHistoryItems.add(
                index = 0,
                element = HistoryItem(
                    attemptId = attemptId,
                    mode = "PRACTICE",
                    title = resolvedTitle,
                    timestamp = currentTimestamp,
                    score = request.score,
                    correctCount = request.correctCount,
                    totalQuestions = total,
                    durationSeconds = request.durationSeconds
                )
            )

            return Result.success(
                SubmitQuizResponse(
                    attemptId = attemptId,
                    earnedXp = earnedXp
                )
            )
        }

        return try {
            val response = topicApi.submitQuizResult(quizId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể lưu kết quả bài thi (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng khi nộp bài."))
        }
    }

    suspend fun getAttemptResult(attemptId: String): Result<QuizResultResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(200L)
            val cached = mockAttempts[attemptId] ?: QuizResultResponse(
                attemptId = attemptId,
                quizId = 1,
                quizTitle = "Công nghệ & AI – Bộ đề #1",
                score = 700,
                earnedXp = 120,
                accuracyPercent = 80,
                performanceTitle = "Tốc độ & Chính xác Tuyệt vời!",
                performanceSubtitle = "Bạn nhanh hơn 84% người chơi cùng chủ đề.",
                durationSeconds = 16,
                correctCount = 4,
                wrongCount = 1,
                totalQuestions = 5
            )
            return Result.success(cached)
        }

        return try {
            val response = topicApi.fetchAttemptResult(attemptId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không tải được kết quả bài thi (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối máy chủ: ${e.localizedMessage}"))
        }
    }

    suspend fun getAttemptDetails(attemptId: String): Result<AttemptDetailResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(200L)
            val cached = mockAttemptDetails[attemptId] ?: AttemptDetailResponse(
                attemptId = attemptId,
                quizTitle = "Công nghệ & AI – Bộ đề #1",
                score = 850,
                maxScore = 1000,
                questions = listOf(
                    QuestionReviewItem(
                        questionNumber = 1,
                        categoryLabel = "Công nghệ & AI",
                        questionContent = "Thuật ngữ \"LLM\" trong lĩnh vực Trí tuệ nhân tạo hiện đại là viết tắt của cụm từ nào?",
                        userAnswerText = "A. Large Language Model",
                        correctAnswerText = "A. Large Language Model",
                        isCorrect = true,
                        explanation = "LLM là viết tắt của Large Language Model (Mô hình Ngôn ngữ Lớn), nền tảng của các trợ lý AI tạo sinh hiện đại."
                    ),
                    QuestionReviewItem(
                        questionNumber = 2,
                        categoryLabel = "Lịch sử Việt Nam",
                        questionContent = "Chiến thắng Bạch Đằng lẫy lừng đánh tan quân Nam Hán của Ngô Quyền diễn ra vào năm nào?",
                        userAnswerText = "A. Năm 938",
                        correctAnswerText = "A. Năm 938",
                        isCorrect = true,
                        explanation = "Năm 938, Ngô Quyền dùng kế đóng cọc gỗ đầu nhọn xuống lòng sông Bạch Đằng, đánh bại quân Nam Hán, mở ra kỷ nguyên độc lập tự chủ lâu dài."
                    ),
                    QuestionReviewItem(
                        questionNumber = 3,
                        categoryLabel = "Khoa học Vũ trụ",
                        questionContent = "Hành tinh nào trong Hệ Mặt Trời có kích thước lớn nhất?",
                        userAnswerText = "A. Sao Thổ (Saturn)",
                        correctAnswerText = "B. Sao Mộc (Jupiter)",
                        isCorrect = false,
                        explanation = "Sao Mộc (Jupiter) là hành tinh khí khổng lồ lớn nhất trong Hệ Mặt Trời với đường kính gấp khoảng 11 lần Trái Đất."
                    ),
                    QuestionReviewItem(
                        questionNumber = 4,
                        categoryLabel = "Công nghệ & AI",
                        questionContent = "Ngôn ngữ lập trình nào được Google khuyến nghị chính thức để phát triển ứng dụng Android?",
                        userAnswerText = "B. Kotlin",
                        correctAnswerText = "B. Kotlin",
                        isCorrect = true,
                        explanation = "Từ năm 2019, Google công bố Kotlin là ngôn ngữ ưu tiên hàng đầu cho phát triển Android nhờ cú pháp ngắn gọn và an toàn null."
                    ),
                    QuestionReviewItem(
                        questionNumber = 5,
                        categoryLabel = "Công nghệ & AI",
                        questionContent = "Giao thức nào giúp mã hóa dữ liệu truyền tải an toàn giữa ứng dụng di động và máy chủ?",
                        userAnswerText = "B. HTTPS",
                        correctAnswerText = "B. HTTPS",
                        isCorrect = true,
                        explanation = "HTTPS (Hypertext Transfer Protocol Secure) sử dụng chứng chỉ SSL/TLS để mã hóa toàn bộ dữ liệu trao đổi trên mạng."
                    )

                )
            )
            return Result.success(cached)
        }

        return try {
            val response = topicApi.fetchAttemptDetails(attemptId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không tải được chi tiết bài làm (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối máy chủ: ${e.localizedMessage}"))
        }
    }
}