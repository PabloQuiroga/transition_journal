package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.repository.JournalRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CreateOrUpdateEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(entry: JournalEntry): Result<Long> {
        if (entry.content.isBlank() && entry.photos.isEmpty()) {
            return Result.failure(IllegalArgumentException("Entry must have content or photos"))
        }

        val finalTitle = if (entry.title.isBlank()) {
            val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")
                .withZone(ZoneId.systemDefault())
            formatter.format(entry.createdAt)
        } else {
            entry.title
        }

        val entryToSave = entry.copy(
            title = finalTitle,
            updatedAt = Instant.now()
        )

        return repository.saveEntry(entryToSave)
    }
}
