package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.api.RetrofitClient
import com.example.doan.api.SubmitQuizRequest
import com.example.doan.model.QuestionItem
import com.example.doan.model.QuestionReviewItem
import com.example.doan.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LamBaiThiUiState(
    val topicTitle: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val questions: List<QuestionItem> = emptyList(),
    val currentIndex: Int = 0,
    val timeLeft: Int = 15,
    val maxTime: Int = 15,
    val totalElapsedSeconds: Int = 0,
    val score: Int = 0,
    val correctCount: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val hiddenOptions: Set<Int> = emptySet(),
    val used5050: Boolean = false,
    val usedSkip: Boolean = false,
    val hasBackupQuestion: Boolean = false,
    val isFinished: Boolean = false,
    val submittedAttemptId: String? = null
) {
    val currentQuestion: QuestionItem?
        get() = questions.getOrNull(currentIndex)
}

class LamBaiThiViewModel(
    private val repository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LamBaiThiUiState())
    val uiState: StateFlow<LamBaiThiUiState> = _uiState.asStateFlow()

    private var currentQuizId: Int = 0
    private var defaultTimePerQuestion: Int = 15
    private var backupQuestion: QuestionItem? = null
    private var timerJob: Job? = null

    private var accumulatedActiveMillis: Long = 0L
    private var questionStartMillis: Long = 0L

    // Lưu lịch sử từng câu đã trả lời để hiển thị tại MH_07 (Chi tiết bài làm)
    private val recordedReviews = mutableListOf<QuestionReviewItem>()

    fun startQuiz(quizId: Int, fallbackTitle: String, forceReload: Boolean = false) {
        if (!forceReload && _uiState.value.questions.isNotEmpty()) return
        currentQuizId = quizId

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    topicTitle = fallbackTitle,
                    isLoading = true,
                    errorMessage = null
                )
            }

            repository.getQuizSession(quizId).fold(
                onSuccess = { session ->
                    if (session.questions.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Bộ đề hiện chưa có câu hỏi trên hệ thống."
                            )
                        }
                        return@launch
                    }

                    defaultTimePerQuestion = session.timePerQuestionSeconds.coerceAtLeast(5)
                    backupQuestion = session.backupQuestion
                    accumulatedActiveMillis = 0L
                    recordedReviews.clear()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            topicTitle = session.topicTitle.ifBlank { fallbackTitle },
                            questions = session.questions,
                            currentIndex = 0,
                            timeLeft = defaultTimePerQuestion,
                            maxTime = defaultTimePerQuestion,
                            totalElapsedSeconds = 0,
                            score = 0,
                            correctCount = 0,
                            selectedOptionIndex = null,
                            isAnswerRevealed = false,
                            hiddenOptions = emptySet(),
                            used5050 = false,
                            usedSkip = false,
                            hasBackupQuestion = session.backupQuestion != null,
                            isFinished = false,
                            submittedAttemptId = null
                        )
                    }
                    startTimer()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message)
                    }
                }
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        questionStartMillis = System.currentTimeMillis()

        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && !_uiState.value.isAnswerRevealed) {
                delay(1000L)
                val currentQuestionMillis = if (questionStartMillis > 0L) {
                    System.currentTimeMillis() - questionStartMillis
                } else {
                    0L
                }
                val totalSeconds = ((accumulatedActiveMillis + currentQuestionMillis) / 1000L).toInt()

                _uiState.update {
                    it.copy(
                        timeLeft = (it.timeLeft - 1).coerceAtLeast(0),
                        totalElapsedSeconds = totalSeconds
                    )
                }
            }
            if (_uiState.value.timeLeft == 0 && !_uiState.value.isAnswerRevealed) {
                revealAndAdvance(selectedIndex = null)
            }
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val state = _uiState.value
        if (state.isAnswerRevealed || optionIndex in state.hiddenOptions) return
        timerJob?.cancel()
        revealAndAdvance(selectedIndex = optionIndex)
    }

    private fun revealAndAdvance(selectedIndex: Int?) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return

        if (questionStartMillis > 0L) {
            accumulatedActiveMillis += (System.currentTimeMillis() - questionStartMillis)
            questionStartMillis = 0L
        }
        val finalElapsedSeconds = (accumulatedActiveMillis / 1000L).toInt().coerceAtLeast(1)

        val isCorrect = selectedIndex == question.correctIndex
        // Câu đúng = 10 điểm + (thời gian dư * 15); câu sai = 0 điểm
        val earnedPoints = if (isCorrect) 10 + (state.timeLeft * 15) else 0

        // Ghi lại chi tiết câu hỏi & đáp án người dùng chọn cho MH_07
        val labels = listOf("A", "B", "C", "D")
        val userChoiceText = selectedIndex?.let { idx ->
            "${labels.getOrElse(idx) { "" }}. ${question.options.getOrElse(idx) { "" }}"
        }
        val correctChoiceText =
            "${labels.getOrElse(question.correctIndex) { "" }}. ${question.options.getOrElse(question.correctIndex) { "" }}"
        val categoryLabel = state.topicTitle.substringBefore("–").trim().ifBlank { "Tổng hợp" }

        recordedReviews.add(
            QuestionReviewItem(
                questionNumber = state.currentIndex + 1,
                categoryLabel = categoryLabel,
                questionContent = question.content,
                userAnswerText = userChoiceText,
                correctAnswerText = correctChoiceText,
                isCorrect = isCorrect,
                explanation = "Đáp án chính xác là $correctChoiceText."
            )
        )

        _uiState.update {
            it.copy(
                selectedOptionIndex = selectedIndex,
                isAnswerRevealed = true,
                totalElapsedSeconds = finalElapsedSeconds,
                score = it.score + earnedPoints,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount
            )
        }

        viewModelScope.launch {
            delay(750L)
            moveToNextQuestion()
        }
    }

    private suspend fun moveToNextQuestion() {
        val latestState = _uiState.value
        if (latestState.currentIndex + 1 >= latestState.questions.size) {
            timerJob?.cancel()
            val submitResponse = repository.submitResult(
                quizId = currentQuizId,
                quizTitle = latestState.topicTitle,
                request = SubmitQuizRequest(
                    userId = RetrofitClient.currentUserId,
                    score = latestState.score,
                    correctCount = latestState.correctCount,
                    totalQuestions = latestState.questions.size,
                    durationSeconds = latestState.totalElapsedSeconds
                ),
                questionReviews = recordedReviews.toList()
            )
            val attemptId = submitResponse.getOrNull()?.attemptId ?: "attempt_default"
            _uiState.update { it.copy(isFinished = true, submittedAttemptId = attemptId) }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    timeLeft = defaultTimePerQuestion,
                    maxTime = defaultTimePerQuestion,
                    selectedOptionIndex = null,
                    isAnswerRevealed = false,
                    hiddenOptions = emptySet()
                )
            }
            startTimer()
        }
    }

    fun useFiftyFifty() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.used5050 || state.isAnswerRevealed) return

        val hidden = question.options.indices
            .filter { it != question.correctIndex }
            .shuffled()
            .take(2)
            .toSet()

        _uiState.update { it.copy(used5050 = true, hiddenOptions = hidden) }
    }

    fun useSkipQuestion() {
        val state = _uiState.value
        val replacement = backupQuestion ?: return
        if (state.usedSkip || state.isAnswerRevealed) return

        if (questionStartMillis > 0L) {
            accumulatedActiveMillis += (System.currentTimeMillis() - questionStartMillis)
        }

        val updatedList = state.questions.toMutableList().apply {
            set(state.currentIndex, replacement)
        }
        backupQuestion = null

        _uiState.update {
            it.copy(
                usedSkip = true,
                hasBackupQuestion = false,
                questions = updatedList,
                timeLeft = defaultTimePerQuestion,
                maxTime = defaultTimePerQuestion,
                hiddenOptions = emptySet()
            )
        }
        startTimer()
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
