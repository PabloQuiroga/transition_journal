package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.repository.PhotoStorageManager
import javax.inject.Inject

data class ShareContent(
    val textContent: String,
    val photoUriString: String? = null
)

class PrepareShareContentUseCase @Inject constructor(
    private val photoStorageManager: PhotoStorageManager
) {
    suspend operator fun invoke(
        entry: JournalEntry,
        selectedPhotoFileName: String? = null
    ): Result<ShareContent> {
        val formattedText = buildString {
            appendLine("✨ ${entry.title}")
            appendLine("Emoción: ${entry.mood?.name ?: "No especificada"}")
            appendLine("Categoría: ${entry.category.name}")
            appendLine()
            appendLine(entry.content)
        }

        val photoUri = if (selectedPhotoFileName != null) {
            val photoResult = photoStorageManager.prepareShareableFile(selectedPhotoFileName)
            if (photoResult.isFailure) {
                return Result.failure(photoResult.exceptionOrNull() ?: Exception("Failed to prepare photo"))
            }
            photoResult.getOrNull()
        } else {
            null
        }

        return Result.success(
            ShareContent(
                textContent = formattedText,
                photoUriString = photoUri
            )
        )
    }
}
