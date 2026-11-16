package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.EntryPhoto
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.JournalRepository
import com.refugio.journal.domain.repository.PhotoStorageManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteEntryUseCaseTest {

    private val repository: JournalRepository = mockk()
    private val photoStorageManager: PhotoStorageManager = mockk(relaxed = true)
    private val useCase = DeleteEntryUseCase(repository, photoStorageManager)

    @Test
    fun `when deleting entry then deletes associated photos and entry in repository`() = runTest {
        val entryId = 10L
        val entry = JournalEntry(
            id = entryId,
            title = "Test",
            content = "Content",
            mood = Mood.TRANQUILA,
            category = Category.EMOCIONAL,
            photos = listOf(
                EntryPhoto(id = 1L, entryId = entryId, fileName = "photo1.jpg", absolutePath = "/path/photo1.jpg")
            )
        )

        coEvery { repository.getEntryById(entryId) } returns entry
        coEvery { repository.deleteEntry(entryId) } returns Result.success(Unit)

        val result = useCase(entryId)

        assertTrue(result.isSuccess)
        coVerify { photoStorageManager.deletePrivatePhoto("photo1.jpg") }
        coVerify { repository.deleteEntry(entryId) }
    }
}
