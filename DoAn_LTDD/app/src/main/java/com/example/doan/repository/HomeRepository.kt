package com.example.doan.repository

import com.example.doan.api.RetrofitClient
import com.example.doan.api.TopicApi
import com.example.doan.model.FeaturedTopic
import com.example.doan.model.TopicIconType
import kotlinx.coroutines.delay

class HomeRepository(
    private val topicApi: TopicApi = RetrofitClient.topicApi
) {
    suspend fun getFeaturedTopics(): List<FeaturedTopic> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(200L)
            return listOf(
                FeaturedTopic(1, "Công nghệ & AI", 50, 72, TopicIconType.TECH),
                FeaturedTopic(2, "Lịch sử Việt Nam", 40, 45, TopicIconType.HISTORY),
                FeaturedTopic(3, "Vũ trụ & Thiên văn", 35, 20, TopicIconType.SPACE)
            )
        }

        return try {
            val response = topicApi.fetchFeaturedTopics()
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}