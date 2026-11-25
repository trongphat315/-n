package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import com.example.doan.model.TopicItem
import com.example.doan.repository.TopicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChuDeUiState(
    val searchQuery: String = "",
    val categories: List<String> = listOf("Tất cả", "Công nghệ", "Khoa học", "Lịch sử", "Tiếng Anh"),
    val selectedCategory: String = "Tất cả",
    val allTopics: List<TopicItem> = emptyList(),
    val filteredTopics: List<TopicItem> = emptyList(),
    val expandedTopicId: Int? = 1 // Mặc định mở rộng chủ đề đầu tiên
)

class ChuDeViewModel(
    private val repository: TopicRepository = TopicRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChuDeUiState())
    val uiState: StateFlow<ChuDeUiState> = _uiState.asStateFlow()

    init {
        val topics = repository.getAllTopics()
        _uiState.update {
            it.copy(allTopics = topics, filteredTopics = topics)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = filterTopics(state.allTopics, query, state.selectedCategory)
            state.copy(searchQuery = query, filteredTopics = filtered)
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { state ->
            val filtered = filterTopics(state.allTopics, state.searchQuery, category)
            state.copy(selectedCategory = category, filteredTopics = filtered)
        }
    }

    fun toggleTopicExpand(topicId: Int) {
        _uiState.update { state ->
            val nextId = if (state.expandedTopicId == topicId) null else topicId
            state.copy(expandedTopicId = nextId)
        }
    }

    private fun filterTopics(
        topics: List<TopicItem>,
        query: String,
        category: String
    ): List<TopicItem> {
        return topics.filter { topic ->
            val matchCategory = category == "Tất cả" || topic.category.equals(category, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                    topic.title.contains(query, ignoreCase = true) ||
                    topic.quizSets.any { it.title.contains(query, ignoreCase = true) }
            matchCategory && matchQuery
        }
    }
}