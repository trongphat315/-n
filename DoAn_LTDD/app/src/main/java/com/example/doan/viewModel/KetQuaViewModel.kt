package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.model.QuizResultResponse
import com.example.doan.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KetQuaUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val result: QuizResultResponse? = null
)

class KetQuaViewModel(
    private val repository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KetQuaUiState())
    val uiState: StateFlow<KetQuaUiState> = _uiState.asStateFlow()

    fun loadResult(attemptId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getAttemptResult(attemptId).fold(
                onSuccess = { data ->
                    _uiState.update { it.copy(isLoading = false, result = data) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }
}