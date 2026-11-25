package com.example.doan.repository

import com.example.doan.api.BattleApi
import com.example.doan.api.InviteFriendRequest
import com.example.doan.api.MatchmakingRequest
import com.example.doan.api.RetrofitClient
import com.example.doan.api.SubmitQuizRequest
import com.example.doan.model.BattleFriendItem
import com.example.doan.model.BattleLobbyResponse
import com.example.doan.model.BattleMatchSessionResponse
import com.example.doan.model.BattleQuestionItem
import com.example.doan.model.MatchmakingResponse
import com.example.doan.model.QuestionReviewItem
import com.example.doan.model.SubmitBattleMatchRequest
import com.example.doan.model.SubmitBattleMatchResponse
import kotlinx.coroutines.delay

class BattleRepository(
    private val battleApi: BattleApi = RetrofitClient.battleApi,
    private val quizRepository: QuizRepository = QuizRepository()
) {
    suspend fun getBattleLobby(): Result<BattleLobbyResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(200L)
            return Result.success(
                BattleLobbyResponse(
                    onlineCount = 1248,
                    currentUserName = "Minh Tuấn",
                    currentUserInitials = "MT",
                    currentUserLevel = 14,
                    questionCount = 5,
                    secondsPerQuestion = 15,
                    roomCode = "#CPN-889",
                    topics = listOf(
                        "Ngẫu nhiên",
                        "Công nghệ & AI",
                        "Lịch sử VN",
                        "Tiếng Anh",
                        "Khoa học"
                    ),
                    friends = listOf(
                        BattleFriendItem(
                            userId = "friend_01",
                            initials = "HN",
                            fullName = "Trần Hoàng Nam",
                            level = 18,
                            statusText = "Trực tuyến",
                            isOnline = true,
                            avatarColorType = "ORANGE"
                        ),
                        BattleFriendItem(
                            userId = "friend_02",
                            initials = "LC",
                            fullName = "Nguyễn Linh Chi",
                            level = 17,
                            statusText = "Đang thi đấu",
                            isOnline = true,
                            avatarColorType = "BLUE"
                        ),
                        BattleFriendItem(
                            userId = "friend_03",
                            initials = "QB",
                            fullName = "Lê Quốc Bảo",
                            level = 16,
                            statusText = "Trực tuyến",
                            isOnline = true,
                            avatarColorType = "GREEN"
                        )
                    )
                )
            )
        }

        return try {
            val response = battleApi.fetchBattleLobby(RetrofitClient.currentUserId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không tải được sảnh thách đấu (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối máy chủ: ${e.localizedMessage}"))
        }
    }

    suspend fun findRandomMatch(topic: String, isInstant: Boolean = false): Result<MatchmakingResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(if (isInstant) 350L else 1200L)
            return Result.success(
                MatchmakingResponse(
                    matchId = "match_${System.currentTimeMillis()}",
                    opponentName = "Hoàng Nam",
                    opponentLevel = 14,
                    topic = topic
                )
            )
        }

        return try {
            val response = battleApi.findRandomMatch(
                MatchmakingRequest(
                    userId = RetrofitClient.currentUserId,
                    topic = topic,
                    isInstant = isInstant
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không tìm thấy trận đấu phù hợp (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng khi ghép cặp."))
        }
    }

    suspend fun inviteFriend(
        friend: BattleFriendItem,
        roomCode: String,
        topic: String
    ): Result<MatchmakingResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(500L)
            return Result.success(
                MatchmakingResponse(
                    matchId = "room_${roomCode.removePrefix("#")}_${friend.userId}",
                    opponentName = friend.fullName,
                    opponentLevel = friend.level,
                    topic = topic
                )
            )
        }

        return try {
            val response = battleApi.inviteFriendToMatch(
                InviteFriendRequest(
                    fromUserId = RetrofitClient.currentUserId,
                    toFriendId = friend.userId,
                    roomCode = roomCode,
                    topic = topic
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể gửi lời mời tới ${friend.fullName}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng khi mời bạn bè."))
        }
    }

    suspend fun getMatchSession(matchId: String): Result<BattleMatchSessionResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(200L)
            val opponentName = when {
                matchId.contains("friend_02") -> "Linh Chi"
                matchId.contains("friend_03") -> "Quốc Bảo"
                else -> "Hoàng Nam"
            }
            val opponentInitials = opponentName.split(" ")
                .takeLast(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifBlank { "HN" }

            return Result.success(
                BattleMatchSessionResponse(
                    matchId = matchId,
                    playerName = "Bạn",
                    playerInitials = "MT",
                    opponentName = opponentName,
                    opponentInitials = opponentInitials,
                    secondsPerQuestion = 15,
                    speedBonusThresholdSeconds = 3,
                    speedBonusPoints = 50,
                    questions = listOf(
                        BattleQuestionItem(
                            id = 1,
                            category = "CÔNG NGHỆ & AI",
                            content = "Thuật ngữ “LLM” trong lĩnh vực Trí tuệ nhân tạo hiện đại là viết tắt của cụm từ nào?",
                            options = listOf(
                                "Large Language Model",
                                "Linear Logic Machine",
                                "Low Latency Memory",
                                "Layered Learning Module"
                            ),
                            correctIndex = 0,
                            explanation = "LLM là viết tắt của Large Language Model (Mô hình Ngôn ngữ Lớn), nền tảng của các trợ lý AI tạo sinh hiện đại."
                        ),
                        BattleQuestionItem(
                            id = 2,
                            category = "LỊCH SỬ VIỆT NAM",
                            content = "Chiến thắng Bạch Đằng lẫy lừng đánh tan quân Nam Hán của Ngô Quyền diễn ra vào năm nào?",
                            options = listOf(
                                "Năm 938",
                                "Năm 981",
                                "Năm 1077",
                                "Năm 1288"
                            ),
                            correctIndex = 0,
                            explanation = "Năm 938, Ngô Quyền dùng kế đóng cọc gỗ đầu nhọn xuống lòng sông Bạch Đằng, đánh bại quân Nam Hán."
                        ),
                        BattleQuestionItem(
                            id = 3,
                            category = "KHOA HỌC VŨ TRỤ",
                            content = "Hành tinh nào trong Hệ Mặt Trời có kích thước lớn nhất?",
                            options = listOf(
                                "Sao Thổ (Saturn)",
                                "Sao Mộc (Jupiter)",
                                "Sao Hải Vương (Neptune)",
                                "Sao Thiên Vương (Uranus)"
                            ),
                            correctIndex = 1,
                            explanation = "Sao Mộc (Jupiter) là hành tinh khí khổng lồ lớn nhất trong Hệ Mặt Trời."
                        ),
                        BattleQuestionItem(
                            id = 4,
                            category = "CÔNG NGHỆ & AI",
                            content = "Ngôn ngữ lập trình nào được Google khuyến nghị chính thức để phát triển Android?",
                            options = listOf(
                                "Swift",
                                "Kotlin",
                                "Ruby",
                                "PHP"
                            ),
                            correctIndex = 1,
                            explanation = "Kotlin là ngôn ngữ lập trình hiện đại được Google ưu tiên cho phát triển Android."
                        ),
                        BattleQuestionItem(
                            id = 5,
                            category = "CÔNG NGHỆ & AI",
                            content = "Giao thức nào giúp mã hóa dữ liệu truyền tải an toàn trên web?",
                            options = listOf(
                                "FTP",
                                "HTTPS",
                                "SMTP",
                                "Telnet"
                            ),
                            correctIndex = 1,
                            explanation = "HTTPS sử dụng chứng chỉ SSL/TLS để mã hóa dữ liệu trao đổi trên mạng."
                        )
                    )
                )
            )
        }

        return try {
            val response = battleApi.fetchBattleMatchSession(matchId, RetrofitClient.currentUserId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không tải được trận đấu 1v1 (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối máy chủ: ${e.localizedMessage}"))
        }
    }

    suspend fun submitMatchResult(
        request: SubmitBattleMatchRequest,
        reviews: List<QuestionReviewItem>
    ): Result<SubmitBattleMatchResponse> {
        if (RetrofitClient.USE_MOCK_DATA) {
            val isVictory = request.playerScore >= request.opponentScore
            val outcomeLabel = if (isVictory) "Thắng" else "Thua"
            val matchTitle = "Đấu 1v1 vs ${request.opponentName} ($outcomeLabel)"

            val savedResult = quizRepository.submitResult(
                quizId = 900,
                quizTitle = matchTitle,
                request = SubmitQuizRequest(
                    userId = request.userId,
                    score = request.playerScore,
                    correctCount = request.correctCount,
                    totalQuestions = request.totalQuestions,
                    durationSeconds = request.durationSeconds
                ),
                questionReviews = reviews,
                mode = "PVP"
            )

            val attemptId = savedResult.getOrNull()?.attemptId ?: "pvp_${System.currentTimeMillis()}"
            val earnedXp = savedResult.getOrNull()?.earnedXp ?: 100

            return Result.success(
                SubmitBattleMatchResponse(
                    attemptId = attemptId,
                    isVictory = isVictory,
                    earnedXp = earnedXp
                )
            )
        }

        return try {
            val response = battleApi.submitBattleMatchResult(request.matchId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể lưu kết quả trận đấu (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng khi kết thúc trận đấu."))
        }
    }
}