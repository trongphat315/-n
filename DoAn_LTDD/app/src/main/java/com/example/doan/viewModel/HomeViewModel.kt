package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.model.FeaturedTopic
import com.example.doan.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "Minh Tuấn",
    val avatarInitials: String = "MT",
    val level: Int = 14,
    val coins: Int = 2450,
    val onlinePlayers: Int = 1248,
    val hasUnreadNotification: Boolean = true,
    val selectedBottomTab: Int = 0,
    val featuredTopics: List<FeaturedTopic> = emptyList()
)

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFeaturedTopics()
    }

    fun loadFeaturedTopics() {
        viewModelScope.launch {
            val topics = repository.getFeaturedTopics()
            _uiState.update { it.copy(featuredTopics = topics) }
        }
    }

    fun setUserName(name: String) {
        if (name.isNotBlank()) {
            val initials = name.trim().split(" ")
                .takeLast(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
            _uiState.update {
                it.copy(userName = name, avatarInitials = initials.ifEmpty { "MT" })
            }
        }
    }

    fun selectBottomTab(index: Int) {
        _uiState.update { it.copy(selectedBottomTab = index) }
    }

    fun clearNotificationBadge() {
        _uiState.update { it.copy(hasUnreadNotification = false) }
    }
}