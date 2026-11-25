package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.model.BattleFriendItem
import com.example.doan.repository.BattleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SanhThachDauUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val onlineCount: Int = 1248,
    val playerName: String = "Minh Tuấn",
    val playerInitials: String = "MT",
    val playerLevel: Int = 14,
    val questionCount: Int = 5,
    val secondsPerQuestion: Int = 15,
    val roomCode: String = "#CPN-889",
    val topics: List<String> = listOf("Ngẫu nhiên", "Công nghệ & AI", "Lịch sử VN", "Tiếng Anh"),
    val selectedTopic: String = "Ngẫu nhiên",
    val friends: List<BattleFriendItem> = emptyList(),
    val isSearchingMatch: Boolean = false,
    val invitingFriendId: String? = null,
    val matchedMatchId: String? = null,
    val bannerNotice: String? = null
)

class SanhThachDauViewModel(
    private val repository: BattleRepository = BattleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SanhThachDauUiState())
    val uiState: StateFlow<SanhThachDauUiState> = _uiState.asStateFlow()

    private var matchmakingJob: Job? = null

    init {
        loadLobby()
    }

    fun loadLobby() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getBattleLobby().fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            onlineCount = data.onlineCount,
                            playerName = data.currentUserName,
                            playerInitials = data.currentUserInitials,
                            playerLevel = data.currentUserLevel,
                            questionCount = data.questionCount,
                            secondsPerQuestion = data.secondsPerQuestion,
                            roomCode = data.roomCode,
                            topics = data.topics,
                            selectedTopic = data.topics.firstOrNull() ?: "Ngẫu nhiên",
                            friends = data.friends
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message)
                    }
                }
            )
        }
    }

    fun selectTopic(topic: String) {
        if (_uiState.value.isSearchingMatch) return
        _uiState.update { it.copy(selectedTopic = topic) }
    }

    fun toggleRandomMatchmaking() {
        if (_uiState.value.isSearchingMatch) {
            matchmakingJob?.cancel()
            _uiState.update {
                it.copy(isSearchingMatch = false, bannerNotice = "Đã hủy tìm trận ngẫu nhiên.")
            }
            return
        }
        startMatchSearch(isInstant = false)
    }

    fun startInstantMatch() {
        if (_uiState.value.isSearchingMatch) return
        startMatchSearch(isInstant = true)
    }

    private fun startMatchSearch(isInstant: Boolean) {
        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isSearchingMatch = true, bannerNotice = null)
            }
            repository.findRandomMatch(_uiState.value.selectedTopic, isInstant).fold(
                onSuccess = { match ->
                    _uiState.update {
                        it.copy(
                            isSearchingMatch = false,
                            matchedMatchId = match.matchId
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSearchingMatch = false,
                            bannerNotice = err.message
                        )
                    }
                }
            )
        }
    }

    fun inviteFriend(friend: BattleFriendItem) {
        if (_uiState.value.invitingFriendId != null || _uiState.value.isSearchingMatch) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(invitingFriendId = friend.userId, bannerNotice = null)
            }
            repository.inviteFriend(
                friend = friend,
                roomCode = _uiState.value.roomCode,
                topic = _uiState.value.selectedTopic
            ).fold(
                onSuccess = { match ->
                    _uiState.update {
                        it.copy(
                            invitingFriendId = null,
                            matchedMatchId = match.matchId
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            invitingFriendId = null,
                            bannerNotice = err.message
                        )
                    }
                }
            )
        }
    }

    fun showCopiedNotice() {
        _uiState.update {
            it.copy(bannerNotice = "Đã sao chép mã phòng ${it.roomCode}!")
        }
    }

    fun consumeMatchNavigation() {
        _uiState.update { it.copy(matchedMatchId = null) }
    }
}