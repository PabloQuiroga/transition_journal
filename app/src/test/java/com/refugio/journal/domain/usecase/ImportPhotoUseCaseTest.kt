package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.repository.PhotoStorageManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportPhotoUseCaseTest {

    private val photoStorageManager: PhotoStorageManager = mockk()
    private val useCase = ImportPhotoUseCase(photoStorageManager)

    @Test
    fun `when current photo count is below limit then imports photo successfully`() = runTest {
        val sourceUri = "content://media/external/images/media/123"
        val expectedFileName = "uuid_photo.jpg"

        coEvery { photoStorageManager.importPhotoToPrivateStorage(sourceUri) } returns Result.success(expectedFileName)

        val result = useCase(sourceUriString = sourceUri, currentPhotoCount = 2)

        assertTrue(result.isSuccess)
        assertEquals(expectedFileName, result.getOrNull())
    }

    @Test
    fun `when current photo count reaches max limit then returns failure result`() = runTest {
        val sourceUri = "content://media/external/images/media/123"

        val result = useCase(sourceUriString = sourceUri, currentPhotoCount = 5)

        assertTrue(result.isFailure)
    }
}
