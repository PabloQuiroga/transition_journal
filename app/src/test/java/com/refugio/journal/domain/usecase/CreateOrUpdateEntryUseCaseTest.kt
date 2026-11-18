package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.EntryPhoto
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.JournalRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateOrUpdateEntryUseCaseTest {

    private val repository: JournalRepository = mockk()
    private val useCase = CreateOrUpdateEntryUseCase(repository)

    @Test
    fun `when entry has no content and no photos then returns failure result`() = runTest {
        val invalidEntry = JournalEntry(
            title = "",
            content = "",
            mood = Mood.TRANQUILA,
            category = Category.EXPRESION_ESTILO,
            photos = emptyList()
        )

        val result = useCase(invalidEntry)

        assertTrue(result.isFailure)
    }

    @Test
    fun `when entry has no content text but contains photo then saves successfully`() = runTest {
        val entry = JournalEntry(
            title = "Look del día",
            content = "",
            mood = Mood.EUFORICA,
            category = Category.EXPRESION_ESTILO,
            photos = listOf(
                EntryPhoto(id = 1L, entryId = 0L, fileName = "outfit.jpg", absolutePath = "/path/outfit.jpg")
            )
        )

        coEvery { repository.saveEntry(any()) } returns Result.success(1L)

        val result = useCase(entry)

        assertTrue(result.isSuccess)
        coVerify { repository.saveEntry(any()) }
    }

    @Test
    fun `when entry has content and blank title then assigns automatic title and saves successfully`() = runTest {
        val entry = JournalEntry(
            title = "",
            content = "Mi nota del día",
            mood = Mood.EUFORICA,
            category = Category.SOCIAL,
            photos = emptyList()
        )
        val capturedSlot = slot<JournalEntry>()
        coEvery { repository.saveEntry(capture(capturedSlot)) } returns Result.success(1L)

        val result = useCase(entry)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        assertTrue(capturedSlot.captured.title.isNotBlank())
    }
}
