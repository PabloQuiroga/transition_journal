package com.refugio.journal.data.local.dao

import androidx.room.Room
import com.refugio.journal.data.local.database.RefugioDatabase
import com.refugio.journal.data.local.entity.JournalEntity
import com.refugio.journal.data.local.entity.PhotoEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class JournalDaoTest {

    private lateinit var database: RefugioDatabase
    private lateinit var dao: JournalDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            RefugioDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.journalDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetEntryWithPhotos() = runTest {
        val entry = JournalEntity(
            id = 1L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            title = "Mi primer hito",
            content = "Contenido de prueba",
            mood = "EUFORICA",
            category = "HITO",
            isMilestone = true
        )
        val photo = PhotoEntity(
            id = 1L,
            entryId = 1L,
            fileName = "photo1.jpg",
            absolutePath = "/path/photo1.jpg"
        )

        dao.insertEntry(entry)
        dao.insertPhoto(photo)

        val retrievedEntry = dao.getEntryById(1L)
        val retrievedPhotos = dao.getPhotosForEntry(1L)

        assertEquals("Mi primer hito", retrievedEntry?.title)
        assertEquals(1, retrievedPhotos.size)
        assertEquals("photo1.jpg", retrievedPhotos[0].fileName)
    }

    @Test
    fun deleteEntryCascadeDeletesPhotos() = runTest {
        val entry = JournalEntity(
            id = 2L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            title = "Para borrar",
            content = "Nota",
            mood = "TRANQUILA",
            category = "EMOCIONAL",
            isMilestone = false
        )
        val photo = PhotoEntity(
            id = 2L,
            entryId = 2L,
            fileName = "photo2.jpg",
            absolutePath = "/path/photo2.jpg"
        )

        dao.insertEntry(entry)
        dao.insertPhoto(photo)
        dao.deleteEntryById(2L)

        assertNull(dao.getEntryById(2L))
        assertEquals(0, dao.getPhotosForEntry(2L).size)
    }
}
