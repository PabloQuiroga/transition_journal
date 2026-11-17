package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.repository.PhotoStorageManager
import javax.inject.Inject

class ImportPhotoUseCase @Inject constructor(
    private val photoStorageManager: PhotoStorageManager
) {
    companion object {
        const val MAX_PHOTOS_PER_ENTRY = 5
    }

    suspend operator fun invoke(sourceUriString: String, currentPhotoCount: Int): Result<String> {
        if (currentPhotoCount >= MAX_PHOTOS_PER_ENTRY) {
            return Result.failure(IllegalStateException("Maximum $MAX_PHOTOS_PER_ENTRY photos allowed per entry"))
        }

        return photoStorageManager.importPhotoToPrivateStorage(sourceUriString)
    }
}
