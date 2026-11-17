package com.refugio.journal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.EntryFilter
import com.refugio.journal.domain.usecase.DeleteEntryUseCase
import com.refugio.journal.domain.usecase.GetFilteredTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimelineUiState(
    val entries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = false,
    val filter: EntryFilter = EntryFilter(),
    val errorMessage: String? = null
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getFilteredTimelineUseCase: GetFilteredTimelineUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState(isLoading = true))
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        observeEntries()
    }

    private fun observeEntries() {
        val currentFilter = _uiState.value.filter
        getFilteredTimelineUseCase(currentFilter)
            .onEach { entries ->
                _uiState.update { it.copy(entries = entries, isLoading = false, errorMessage = null) }
            }
            .catch { throwable ->
                _uiState.update { it.copy(isLoading = false, errorMessage = throwable.localizedMessage ?: "Error desconocido") }
            }
            .launchIn(viewModelScope)
    }

    fun updateFilter(newFilter: EntryFilter) {
        _uiState.update { it.copy(filter = newFilter, isLoading = true) }
        observeEntries()
    }

    fun setCategory(category: Category?) {
        updateFilter(_uiState.value.filter.copy(category = category))
    }

    fun setMood(mood: Mood?) {
        updateFilter(_uiState.value.filter.copy(mood = mood))
    }

    fun setMilestoneOnly(isMilestone: Boolean) {
        updateFilter(_uiState.value.filter.copy(isMilestoneOnly = isMilestone))
    }

    fun setQueryText(query: String?) {
        updateFilter(_uiState.value.filter.copy(queryText = query))
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            deleteEntryUseCase(id).onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Error al eliminar entrada") }
            }
        }
    }
}
