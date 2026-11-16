package com.refugio.journal.domain.repository

interface PhotoStorageManager {
    suspend fun importPhotoToPrivateStorage(sourceUriString: String): Result<String>
    suspend fun deletePrivatePhoto(fileName: String): Result<Unit>
    suspend fun prepareShareableFile(fileName: String): Result<String>
    fun clearShareCache()
}
