package com.example.doan.model

data class BattleFriendItem(
    val userId: String,
    val initials: String,
    val fullName: String,
    val level: Int,
    val statusText: String,
    val isOnline: Boolean,
    val avatarColorType: String // "ORANGE", "BLUE", "GREEN"
)

data class BattleLobbyResponse(
    val onlineCount: Int,
    val currentUserName: String,
    val currentUserInitials: String,
    val currentUserLevel: Int,
    val questionCount: Int,
    val secondsPerQuestion: Int,
    val roomCode: String,
    val topics: List<String>,
    val friends: List<BattleFriendItem>
)

data class MatchmakingResponse(
    val matchId: String,
    val opponentName: String,
    val opponentLevel: Int,
    val topic: String
)