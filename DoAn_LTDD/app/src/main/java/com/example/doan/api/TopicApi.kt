package com.example.doan.api

import com.example.doan.model.AttemptDetailResponse
import com.example.doan.model.FeaturedTopic
import com.example.doan.model.HistoryResponse
import com.example.doan.model.QuizResultResponse
import com.example.doan.model.QuizSessionResponse
import com.example.doan.model.TopicItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class SubmitQuizRequest(
    val userId: String,
    val score: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val durationSeconds: Int
)

data class SubmitQuizResponse(
    val attemptId: String,
    val earnedXp: Int
)

interface TopicApi {

    @GET("topics/featured")
    suspend fun fetchFeaturedTopics(): Response<List<FeaturedTopic>>

    @GET("topics")
    suspend fun fetchAllTopics(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<List<TopicItem>>

    @GET("quizzes/{quizId}/questions")
    suspend fun fetchQuizSession(
        @Path("quizId") quizId: Int
    ): Response<QuizSessionResponse>

    @POST("quizzes/{quizId}/submit")
    suspend fun submitQuizResult(
        @Path("quizId") quizId: Int,
        @Body body: SubmitQuizRequest
    ): Response<SubmitQuizResponse>

    @GET("quizzes/attempts/{attemptId}")
    suspend fun fetchAttemptResult(
        @Path("attemptId") attemptId: String
    ): Response<QuizResultResponse>
    @GET("history")
    suspend fun fetchUserHistory(
        @Query("userId") userId: String,
        @Query("mode") mode: String? = null
    ): Response<HistoryResponse>
    @GET("quizzes/attempts/{attemptId}/details")
    suspend fun fetchAttemptDetails(
        @Path("attemptId") attemptId: String
    ): Response<AttemptDetailResponse>
}