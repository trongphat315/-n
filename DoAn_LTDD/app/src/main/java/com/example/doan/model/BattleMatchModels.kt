package com.example.doan.model

enum class QuestionDotStatus {
    PENDING,
    CURRENT,
    CORRECT,
    WRONG
}

data class BattleQuestionItem(
    val id: Int,
    val category: String,
    val content: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class BattleMatchSessionResponse(
    val matchId: String,
    val playerName: String,
    val playerInitials: String,
    val opponentName: String,
    val opponentInitials: String,
    val secondsPerQuestion: Int,
    val speedBonusThresholdSeconds: Int,
    val speedBonusPoints: Int,
    val questions: List<BattleQuestionItem>
)

data class SubmitBattleMatchRequest(
    val matchId: String,
    val userId: String,
    val opponentName: String,
    val playerScore: Int,
    val opponentScore: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val durationSeconds: Int
)

data class SubmitBattleMatchResponse(
    val attemptId: String,
    val isVictory: Boolean,
    val earnedXp: Int
)