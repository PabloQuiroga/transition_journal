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
import java.time.Instant

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
                EntryPhoto(id = 1L, entryId = 0L, fileName = "outfit.jpg")
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

    @Test
    fun `when entry has explicit title then preserves explicit title without overwriting`() = runTest {
        val entry = JournalEntry(
            title = "Título Personalizado",
            content = "Nota con título propio",
            mood = Mood.TRANQUILA,
            category = Category.EMOCIONAL,
            photos = emptyList()
        )
        val capturedSlot = slot<JournalEntry>()
        coEvery { repository.saveEntry(capture(capturedSlot)) } returns Result.success(1L)

        val result = useCase(entry)

        assertTrue(result.isSuccess)
        assertEquals("Título Personalizado", capturedSlot.captured.title)
    }

    @Test
    fun `when updating existing entry then preserves createdAt and updates updatedAt`() = runTest {
        val originalCreatedAt = Instant.now().minusSeconds(86400)
        val oldUpdatedAt = Instant.now().minusSeconds(43200)
        val existingEntry = JournalEntry(
            id = 5L,
            createdAt = originalCreatedAt,
            updatedAt = oldUpdatedAt,
            title = "Entrada existente",
            content = "Contenido editado",
            mood = Mood.TRANQUILA,
            category = Category.EMOCIONAL,
            photos = emptyList()
        )
        val capturedSlot = slot<JournalEntry>()
        coEvery { repository.saveEntry(capture(capturedSlot)) } returns Result.success(5L)

        val result = useCase(existingEntry)

        assertTrue(result.isSuccess)
        assertEquals(originalCreatedAt, capturedSlot.captured.createdAt)
        assertTrue(capturedSlot.captured.updatedAt.isAfter(oldUpdatedAt))
    }

    @Test
    fun `when creating new entry with past custom createdAt then preserves past createdAt date`() = runTest {
        val pastDate = Instant.now().minusSeconds(86400 * 30)
        val pastEntry = JournalEntry(
            id = 0L,
            createdAt = pastDate,
            title = "Registro de hace un mes",
            content = "Nota retroactiva",
            mood = Mood.TRANQUILA,
            category = Category.EMOCIONAL,
            photos = emptyList()
        )
        val capturedSlot = slot<JournalEntry>()
        coEvery { repository.saveEntry(capture(capturedSlot)) } returns Result.success(10L)

        val result = useCase(pastEntry)

        assertTrue(result.isSuccess)
        assertEquals(pastDate, capturedSlot.captured.createdAt)
    }
}
