package com.refugio.journal.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.repository.JournalRepository
import com.refugio.journal.domain.usecase.DeleteEntryUseCase
import com.refugio.journal.domain.usecase.PrepareShareContentUseCase
import com.refugio.journal.domain.usecase.ShareContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EntryDetailUiState(
    val entry: JournalEntry? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val shareContent: ShareContent? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val journalRepository: JournalRepository,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val prepareShareContentUseCase: PrepareShareContentUseCase
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: 0L

    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        loadEntry()
    }

    fun loadEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val entry = journalRepository.getEntryById(entryId)
            if (entry != null) {
                _uiState.update { it.copy(entry = entry, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Entrada no encontrada") }
            }
        }
    }

    fun prepareShare(selectedPhotoFileName: String? = null) {
        viewModelScope.launch {
            val currentEntry = _uiState.value.entry ?: return@launch
            prepareShareContentUseCase(currentEntry, selectedPhotoFileName)
                .onSuccess { shareContent ->
                    _uiState.update { it.copy(shareContent = shareContent) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Error al preparar contenido") }
                }
        }
    }

    fun clearShareContent() {
        _uiState.update { it.copy(shareContent = null) }
    }

    fun deleteEntry() {
        viewModelScope.launch {
            deleteEntryUseCase(entryId)
                .onSuccess {
                    _uiState.update { it.copy(isDeleted = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Error al eliminar") }
                }
        }
    }
}
