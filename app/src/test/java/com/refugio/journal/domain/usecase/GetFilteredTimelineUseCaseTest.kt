package com.refugio.journal.domain.usecase

import app.cash.turbine.test
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.EntryFilter
import com.refugio.journal.domain.repository.JournalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFilteredTimelineUseCaseTest {

    private val repository: JournalRepository = mockk()
    private val useCase = GetFilteredTimelineUseCase(repository)

    @Test
    fun `when filtering entries then delegates to repository with filter`() = runTest {
        val filter = EntryFilter(category = Category.EXPRESION_ESTILO)
        val mockEntries = listOf(
            JournalEntry(
                id = 1L,
                title = "Look de hoy",
                content = "Probando nuevo outfit",
                mood = Mood.EUFORICA,
                category = Category.EXPRESION_ESTILO,
                isMilestone = false
            )
        )

        every { repository.observeEntries(filter) } returns flowOf(mockEntries)

        useCase(filter).test {
            val emitted = awaitItem()
            assertEquals(1, emitted.size)
            assertEquals("Look de hoy", emitted[0].title)
            awaitComplete()
        }
    }
}
