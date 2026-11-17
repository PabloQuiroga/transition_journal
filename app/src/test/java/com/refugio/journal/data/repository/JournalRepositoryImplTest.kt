package com.refugio.journal.data.repository

import com.refugio.journal.data.local.dao.JournalDao
import com.refugio.journal.data.local.entity.JournalEntity
import com.refugio.journal.data.local.entity.PhotoEntity
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class JournalRepositoryImplTest {

    private val journalDao: JournalDao = mockk()
    private val repository = JournalRepositoryImpl(journalDao)

    @Test
    fun `getEntryById returns mapped domain model with photos`() = runTest {
        val entryId = 1L
        val journalEntity = JournalEntity(
            id = entryId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            title = "Test Title",
            content = "Test Content",
            mood = "EUFORICA",
            category = "SOCIAL",
            isMilestone = true
        )
        val photoEntity = PhotoEntity(
            id = 10L,
            entryId = entryId,
            fileName = "photo.jpg",
            absolutePath = "/path/photo.jpg"
        )

        coEvery { journalDao.getEntryById(entryId) } returns journalEntity
        coEvery { journalDao.getPhotosForEntry(entryId) } returns listOf(photoEntity)

        val result = repository.getEntryById(entryId)

        assertNotNull(result)
        assertEquals("Test Title", result?.title)
        assertEquals(Mood.EUFORICA, result?.mood)
        assertEquals(1, result?.photos?.size)
        assertEquals("photo.jpg", result?.photos?.get(0)?.fileName)
    }

    @Test
    fun `saveEntry inserts entity and associated photos atomically`() = runTest {
        val entry = JournalEntry(
            id = 0L,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            title = "Nuevo hito",
            content = "Texto",
            mood = Mood.EMPODERADA,
            category = Category.HITO,
            isMilestone = true
        )

        coEvery { journalDao.insertEntry(any()) } returns 5L
        coEvery { journalDao.insertPhotos(any()) } returns Unit

        val result = repository.saveEntry(entry)

        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull())
        coVerify { journalDao.insertEntry(any()) }
    }
}
