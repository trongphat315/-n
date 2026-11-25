package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.api.RetrofitClient
import com.example.doan.model.BattleQuestionItem
import com.example.doan.model.QuestionDotStatus
import com.example.doan.model.QuestionReviewItem
import com.example.doan.model.SubmitBattleMatchRequest
import com.example.doan.repository.BattleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ThiDau1v1UiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val playerName: String = "Bạn",
    val playerInitials: String = "MT",
    val playerScore: Int = 0,
    val playerDots: List<QuestionDotStatus> = List(5) { QuestionDotStatus.PENDING },
    val opponentName: String = "Hoàng Nam",
    val opponentInitials: String = "HN",
    val opponentScore: Int = 0,
    val opponentDots: List<QuestionDotStatus> = List(5) { QuestionDotStatus.PENDING },
    val timeLeft: Int = 15,
    val maxTime: Int = 15,
    val speedBonusThreshold: Int = 3,
    val speedBonusPoints: Int = 50,
    val questions: List<BattleQuestionItem> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val lastSpeedBonusEarned: Boolean = false,
    val isMatchFinished: Boolean = false,
    val submittedAttemptId: String? = null
) {
    val currentQuestion: BattleQuestionItem?
        get() = questions.getOrNull(currentIndex)
}

class ThiDau1v1ViewModel(
    private val repository: BattleRepository = BattleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThiDau1v1UiState())
    val uiState: StateFlow<ThiDau1v1UiState> = _uiState.asStateFlow()

    private var currentMatchId: String = ""
    private var defaultSeconds: Int = 15
    private var timerJob: Job? = null
    private var opponentJob: Job? = null
    private var matchStartMillis: Long = 0L
    private var questionStartMillis: Long = 0L
    private var correctCount: Int = 0
    private var opponentAnsweredCurrent: Boolean = false
    private val recordedReviews = mutableListOf<QuestionReviewItem>()

    fun startMatch(matchId: String) {
        if (_uiState.value.questions.isNotEmpty() && currentMatchId == matchId) return
        currentMatchId = matchId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getMatchSession(matchId).fold(
                onSuccess = { session ->
                    if (session.questions.isEmpty()) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Trận đấu chưa có dữ liệu câu hỏi.")
                        }
                        return@launch
                    }

                    defaultSeconds = session.secondsPerQuestion.coerceAtLeast(5)
                    val total = session.questions.size
                    val initialPlayerDots = List(total) { idx ->
                        if (idx == 0) QuestionDotStatus.CURRENT else QuestionDotStatus.PENDING
                    }
                    val initialOpponentDots = List(total) { idx ->
                        if (idx == 0) QuestionDotStatus.CURRENT else QuestionDotStatus.PENDING
                    }

                    matchStartMillis = System.currentTimeMillis()
                    correctCount = 0
                    recordedReviews.clear()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            playerName = session.playerName,
                            playerInitials = session.playerInitials,
                            playerScore = 0,
                            playerDots = initialPlayerDots,
                            opponentName = session.opponentName,
                            opponentInitials = session.opponentInitials,
                            opponentScore = 0,
                            opponentDots = initialOpponentDots,
                            timeLeft = defaultSeconds,
                            maxTime = defaultSeconds,
                            speedBonusThreshold = session.speedBonusThresholdSeconds,
                            speedBonusPoints = session.speedBonusPoints,
                            questions = session.questions,
                            currentIndex = 0,
                            selectedOptionIndex = null,
                            isAnswerRevealed = false,
                            lastSpeedBonusEarned = false,
                            isMatchFinished = false,
                            submittedAttemptId = null
                        )
                    }
                    startRoundTimers()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message)
                    }
                }
            )
        }
    }

    private fun startRoundTimers() {
        timerJob?.cancel()
        opponentJob?.cancel()
        questionStartMillis = System.currentTimeMillis()
        opponentAnsweredCurrent = false

        // Đồng hồ đếm ngược 15s của câu hiện tại
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && !_uiState.value.isAnswerRevealed) {
                delay(1000L)
                _uiState.update {
                    it.copy(timeLeft = (it.timeLeft - 1).coerceAtLeast(0))
                }
            }
            if (_uiState.value.timeLeft == 0 && !_uiState.value.isAnswerRevealed) {
                revealAndAdvance(selectedIndex = null)
            }
        }

        // Mô phỏng đối thủ trả lời trực tiếp trong ván đấu
        opponentJob = viewModelScope.launch {
            val thinkMillis = Random.nextLong(3200L, 7500L)
            delay(thinkMillis)
            if (!opponentAnsweredCurrent) {
                applyOpponentAnswer(thinkMillis)
            }
        }
    }

    private fun applyOpponentAnswer(elapsedMillis: Long) {
        if (opponentAnsweredCurrent) return
        opponentAnsweredCurrent = true

        val state = _uiState.value
        val idx = state.currentIndex
        val opponentIsCorrect = Random.nextFloat() < 0.72f
        val remainingSec = (state.maxTime - (elapsedMillis / 1000L).toInt()).coerceAtLeast(0)
        val oppPoints = if (opponentIsCorrect) 10 + (remainingSec * 15) else 0

        val updatedOppDots = state.opponentDots.toMutableList().apply {
            if (idx in indices) {
                set(idx, if (opponentIsCorrect) QuestionDotStatus.CORRECT else QuestionDotStatus.WRONG)
            }
        }

        _uiState.update {
            it.copy(
                opponentScore = it.opponentScore + oppPoints,
                opponentDots = updatedOppDots
            )
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val state = _uiState.value
        if (state.isAnswerRevealed) return
        timerJob?.cancel()
        revealAndAdvance(selectedIndex = optionIndex)
    }

    private fun revealAndAdvance(selectedIndex: Int?) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val idx = state.currentIndex

        val elapsedMillis = (System.currentTimeMillis() - questionStartMillis).coerceAtLeast(0L)
        val isCorrect = selectedIndex == question.correctIndex
        val gotSpeedBonus = isCorrect && elapsedMillis <= (state.speedBonusThreshold * 1000L)

        // Điểm cơ bản + thời gian dư + thưởng tốc độ dưới 3s (+50 điểm)
        val basePoints = if (isCorrect) 10 + (state.timeLeft * 15) else 0
        val bonusPoints = if (gotSpeedBonus) state.speedBonusPoints else 0
        val earnedPoints = basePoints + bonusPoints

        if (isCorrect) correctCount++

        val updatedPlayerDots = state.playerDots.toMutableList().apply {
            if (idx in indices) {
                set(idx, if (isCorrect) QuestionDotStatus.CORRECT else QuestionDotStatus.WRONG)
            }
        }

        // Nếu đối thủ chưa trả lời câu này thì chốt kết quả câu này của đối thủ
        if (!opponentAnsweredCurrent) {
            applyOpponentAnswer(Random.nextLong(4000L, 9000L))
        }

        val labels = listOf("A", "B", "C", "D")
        val userChoiceText = selectedIndex?.let {
            "${labels.getOrElse(it) { "" }}. ${question.options.getOrElse(it) { "" }}"
        }
        val correctChoiceText =
            "${labels.getOrElse(question.correctIndex) { "" }}. ${question.options.getOrElse(question.correctIndex) { "" }}"

        recordedReviews.add(
            QuestionReviewItem(
                questionNumber = idx + 1,
                categoryLabel = question.category,
                questionContent = question.content,
                userAnswerText = userChoiceText,
                correctAnswerText = correctChoiceText,
                isCorrect = isCorrect,
                explanation = question.explanation
            )
        )

        _uiState.update {
            it.copy(
                selectedOptionIndex = selectedIndex,
                isAnswerRevealed = true,
                lastSpeedBonusEarned = gotSpeedBonus,
                playerScore = it.playerScore + earnedPoints,
                playerDots = updatedPlayerDots
            )
        }

        viewModelScope.launch {
            delay(850L)
            moveToNextRound()
        }
    }

    private suspend fun moveToNextRound() {
        val latest = _uiState.value
        val nextIndex = latest.currentIndex + 1

        if (nextIndex >= latest.questions.size) {
            timerJob?.cancel()
            opponentJob?.cancel()
            val totalDurationSeconds = ((System.currentTimeMillis() - matchStartMillis) / 1000L)
                .toInt()
                .coerceAtLeast(1)

            val response = repository.submitMatchResult(
                request = SubmitBattleMatchRequest(
                    matchId = currentMatchId,
                    userId = RetrofitClient.currentUserId,
                    opponentName = latest.opponentName,
                    playerScore = latest.playerScore,
                    opponentScore = latest.opponentScore,
                    correctCount = correctCount,
                    totalQuestions = latest.questions.size,
                    durationSeconds = totalDurationSeconds
                ),
                reviews = recordedReviews.toList()
            )

            val attemptId = response.getOrNull()?.attemptId ?: "attempt_default"
            _uiState.update {
                it.copy(
                    isMatchFinished = true,
                    submittedAttemptId = attemptId
                )
            }
        } else {
            val nextPlayerDots = latest.playerDots.toMutableList().apply {
                if (nextIndex in indices) set(nextIndex, QuestionDotStatus.CURRENT)
            }
            val nextOpponentDots = latest.opponentDots.toMutableList().apply {
                if (nextIndex in indices) set(nextIndex, QuestionDotStatus.CURRENT)
            }

            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    timeLeft = defaultSeconds,
                    maxTime = defaultSeconds,
                    selectedOptionIndex = null,
                    isAnswerRevealed = false,
                    lastSpeedBonusEarned = false,
                    playerDots = nextPlayerDots,
                    opponentDots = nextOpponentDots
                )
            }
            startRoundTimers()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        opponentJob?.cancel()
        super.onCleared()
    }
}