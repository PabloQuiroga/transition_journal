package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.EntryPhoto
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.PhotoStorageManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepareShareContentUseCaseTest {

    private val photoStorageManager: PhotoStorageManager = mockk()
    private val useCase = PrepareShareContentUseCase(photoStorageManager)

    @Test
    fun `when sharing text only then formats text and returns null photo uri`() = runTest {
        val entry = JournalEntry(
            id = 1L,
            title = "Un gran paso",
            content = "Hoy compartí mi nombre con mis amigos.",
            mood = Mood.EMPODERADA,
            category = Category.SOCIAL
        )

        val result = useCase(entry = entry, selectedPhotoFileName = null)

        assertTrue(result.isSuccess)
        val shareContent = result.getOrNull()
        assertTrue(shareContent?.textContent?.contains("Un gran paso") == true)
        assertNull(shareContent?.photoUriString)
    }

    @Test
    fun `when sharing with selected photo then calls photoStorageManager to prepare shareable URI`() = runTest {
        val entry = JournalEntry(
            id = 1L,
            title = "Look especial",
            content = "Foto de hoy",
            mood = Mood.EUFORICA,
            category = Category.EXPRESION_ESTILO,
            photos = listOf(EntryPhoto(id = 1L, entryId = 1L, fileName = "photo.jpg"))
        )
        val expectedUri = "content://com.refugio.journal.fileprovider/cache/photo.jpg"

        coEvery { photoStorageManager.prepareShareableFile("photo.jpg") } returns Result.success(expectedUri)

        val result = useCase(entry = entry, selectedPhotoFileName = "photo.jpg")

        assertTrue(result.isSuccess)
        val shareContent = result.getOrNull()
        assertEquals(expectedUri, shareContent?.photoUriString)
    }

    @Test
    fun `when selected photo does not exist in storage then returns failure result`() = runTest {
        val entry = JournalEntry(
            id = 1L,
            title = "Look especial",
            content = "Foto eliminada",
            mood = Mood.EUFORICA,
            category = Category.EXPRESION_ESTILO,
            photos = listOf(EntryPhoto(id = 1L, entryId = 1L, fileName = "missing.jpg"))
        )

        coEvery { photoStorageManager.prepareShareableFile("missing.jpg") } returns Result.failure(IllegalStateException("File not found"))

        val result = useCase(entry = entry, selectedPhotoFileName = "missing.jpg")

        assertTrue(result.isFailure)
    }
}
