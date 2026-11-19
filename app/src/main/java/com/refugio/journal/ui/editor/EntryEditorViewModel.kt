package com.refugio.journal.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.EntryPhoto
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.JournalRepository
import com.refugio.journal.domain.usecase.CreateOrUpdateEntryUseCase
import com.refugio.journal.domain.usecase.ImportPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class EditorUiState(
    val title: String = "",
    val content: String = "",
    val mood: Mood? = null,
    val category: Category? = null,
    val isMilestone: Boolean = false,
    val customCreatedAt: Instant? = null,
    val photos: List<EntryPhoto> = emptyList(),
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EntryEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val journalRepository: JournalRepository,
    private val createOrUpdateEntryUseCase: CreateOrUpdateEntryUseCase,
    private val importPhotoUseCase: ImportPhotoUseCase
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: 0L

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (entryId != 0L) {
            loadExistingEntry(entryId)
        }
    }

    private fun loadExistingEntry(id: Long) {
        viewModelScope.launch {
            val entry = journalRepository.getEntryById(id)
            if (entry != null) {
                _uiState.update {
                    it.copy(
                        title = entry.title,
                        content = entry.content,
                        mood = entry.mood,
                        category = entry.category,
                        isMilestone = entry.isMilestone,
                        customCreatedAt = entry.occurredAt,
                        photos = entry.photos
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun setMood(mood: Mood?) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun setCategory(category: Category?) {
        _uiState.update { it.copy(category = category) }
    }

    fun setMilestone(isMilestone: Boolean) {
        _uiState.update { it.copy(isMilestone = isMilestone) }
    }

    fun setCustomCreatedAt(instant: Instant?) {
        _uiState.update { it.copy(customCreatedAt = instant) }
    }

    fun importPhoto(sourceUriString: String) {
        viewModelScope.launch {
            val currentCount = _uiState.value.photos.size
            importPhotoUseCase(sourceUriString, currentCount)
                .onSuccess { fileName ->
                    val newPhoto = EntryPhoto(
                        entryId = entryId,
                        fileName = fileName,
                        orderIndex = _uiState.value.photos.size
                    )
                    _uiState.update { it.copy(photos = it.photos + newPhoto, errorMessage = null) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Error al importar foto") }
                }
        }
    }

    fun removePhoto(fileName: String) {
        _uiState.update { state ->
            val updatedPhotos = state.photos.filterNot { it.fileName == fileName }
            state.copy(photos = updatedPhotos)
        }
    }

    fun saveEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val state = _uiState.value
            val entry = JournalEntry(
                id = entryId,
                createdAt = state.customCreatedAt ?: Instant.now(),
                occurredAt = state.customCreatedAt ?: Instant.now(),
                title = state.title,
                content = state.content,
                mood = state.mood,
                category = state.category,
                isMilestone = state.isMilestone,
                photos = state.photos
            )

            createOrUpdateEntryUseCase(entry)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.localizedMessage ?: "Error al guardar entrada") }
                }
        }
    }
}
