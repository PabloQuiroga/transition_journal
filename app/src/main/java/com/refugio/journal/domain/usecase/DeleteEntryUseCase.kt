package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.repository.JournalRepository
import com.refugio.journal.domain.repository.PhotoStorageManager
import javax.inject.Inject

class DeleteEntryUseCase @Inject constructor(
    private val repository: JournalRepository,
    private val photoStorageManager: PhotoStorageManager
) {
    suspend operator fun invoke(entryId: Long): Result<Unit> {
        val entry = repository.getEntryById(entryId) ?: return Result.success(Unit)

        for (photo in entry.photos) {
            photoStorageManager.deletePrivatePhoto(photo.fileName)
        }

        return repository.deleteEntry(entryId)
    }
}
