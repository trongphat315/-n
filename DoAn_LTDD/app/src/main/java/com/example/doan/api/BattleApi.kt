package com.example.doan.api

import com.example.doan.model.BattleLobbyResponse
import com.example.doan.model.BattleMatchSessionResponse
import com.example.doan.model.MatchmakingResponse
import com.example.doan.model.SubmitBattleMatchRequest
import com.example.doan.model.SubmitBattleMatchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class MatchmakingRequest(
    val userId: String,
    val topic: String,
    val isInstant: Boolean = false
)

data class InviteFriendRequest(
    val fromUserId: String,
    val toFriendId: String,
    val roomCode: String,
    val topic: String
)

interface BattleApi {

    @GET("battle/lobby")
    suspend fun fetchBattleLobby(
        @Query("userId") userId: String
    ): Response<BattleLobbyResponse>

    @POST("battle/matchmaking")
    suspend fun findRandomMatch(
        @Body body: MatchmakingRequest
    ): Response<MatchmakingResponse>

    @POST("battle/invite")
    suspend fun inviteFriendToMatch(
        @Body body: InviteFriendRequest
    ): Response<MatchmakingResponse>
    @GET("battle/matches/{matchId}")
    suspend fun fetchBattleMatchSession(
        @Path("matchId") matchId: String,
        @Query("userId") userId: String
    ): Response<BattleMatchSessionResponse>

    @POST("battle/matches/{matchId}/submit")
    suspend fun submitBattleMatchResult(
        @Path("matchId") matchId: String,
        @Body body: SubmitBattleMatchRequest
    ): Response<SubmitBattleMatchResponse>
}