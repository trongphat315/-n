package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.model.HistoryItem
import com.example.doan.model.HistorySummary
import com.example.doan.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryFilter(val label: String, val apiMode: String?) {
    ALL("Tất cả", null),
    PRACTICE("Luyện tập", "PRACTICE"),
    PVP("Đấu 1v1", "PVP")
}

data class LichSuUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val summary: HistorySummary = HistorySummary(0, 0, 0),
    val items: List<HistoryItem> = emptyList()
)

class LichSuViewModel(
    private val repository: HistoryRepository = HistoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LichSuUiState())
    val uiState: StateFlow<LichSuUiState> = _uiState.asStateFlow()

    init {
        loadHistory(HistoryFilter.ALL)
    }

    fun selectFilter(filter: HistoryFilter) {
        if (_uiState.value.selectedFilter == filter && _uiState.value.items.isNotEmpty()) return
        loadHistory(filter)
    }

    fun loadHistory(filter: HistoryFilter = _uiState.value.selectedFilter) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedFilter = filter,
                    isLoading = true,
                    errorMessage = null
                )
            }
            repository.getUserHistory(filter.apiMode).fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = data.summary,
                            items = data.items
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message
                        )
                    }
                }
            )
        }
    }
}