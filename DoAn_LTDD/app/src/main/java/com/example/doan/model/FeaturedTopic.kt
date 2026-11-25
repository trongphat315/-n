package com.example.doan.model

enum class TopicIconType { TECH, HISTORY, SPACE }

data class FeaturedTopic(
    val id: Int,
    val title: String,
    val questionCount: Int,
    val completionPercent: Int,
    val iconType: TopicIconType
)