package com.refugio.journal.ui.timeline

import app.cash.turbine.test
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.usecase.DeleteEntryUseCase
import com.refugio.journal.domain.usecase.GetFilteredTimelineUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getFilteredTimelineUseCase: GetFilteredTimelineUseCase = mockk()
    private val deleteEntryUseCase: DeleteEntryUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized then observes filtered entries and emits to uiState`() = runTest {
        val mockEntries = listOf(
            JournalEntry(id = 1L, title = "Hito 1", content = "Contenido", mood = Mood.EUFORICA, category = Category.HITO, isMilestone = true)
        )
        every { getFilteredTimelineUseCase(any()) } returns flowOf(mockEntries)

        val viewModel = TimelineViewModel(getFilteredTimelineUseCase, deleteEntryUseCase)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading || initialState.entries.isEmpty())

            testDispatcher.scheduler.advanceUntilIdle()

            val loadedState = awaitItem()
            assertEquals(1, loadedState.entries.size)
            assertEquals("Hito 1", loadedState.entries[0].title)
        }
    }
}
