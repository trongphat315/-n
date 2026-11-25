package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.model.QuestionReviewItem
import com.example.doan.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DetailFilter {
    ALL,
    CORRECT,
    WRONG
}

data class ChiTietLichSuUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val quizTitle: String = "",
    val score: Int = 0,
    val maxScore: Int = 0,
    val selectedFilter: DetailFilter = DetailFilter.ALL,
    val allQuestions: List<QuestionReviewItem> = emptyList()
) {
    val totalCount: Int get() = allQuestions.size
    val correctCount: Int get() = allQuestions.count { it.isCorrect }
    val wrongCount: Int get() = allQuestions.count { !it.isCorrect }

    val filteredQuestions: List<QuestionReviewItem>
        get() = when (selectedFilter) {
            DetailFilter.ALL -> allQuestions
            DetailFilter.CORRECT -> allQuestions.filter { it.isCorrect }
            DetailFilter.WRONG -> allQuestions.filter { !it.isCorrect }
        }
}

class ChiTietLichSuViewModel(
    private val repository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChiTietLichSuUiState())
    val uiState: StateFlow<ChiTietLichSuUiState> = _uiState.asStateFlow()

    fun loadAttemptDetails(attemptId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getAttemptDetails(attemptId).fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quizTitle = data.quizTitle,
                            score = data.score,
                            maxScore = data.maxScore,
                            allQuestions = data.questions
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }

    fun selectFilter(filter: DetailFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
