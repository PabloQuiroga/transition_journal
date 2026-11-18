package com.refugio.journal.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.JournalRepository
import com.refugio.journal.domain.usecase.DeleteEntryUseCase
import com.refugio.journal.domain.usecase.PrepareShareContentUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val journalRepository: JournalRepository = mockk()
    private val deleteEntryUseCase: DeleteEntryUseCase = mockk()
    private val prepareShareContentUseCase: PrepareShareContentUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized with entryId then loads entry successfully`() = runTest {
        val entryId = 1L
        val mockEntry = JournalEntry(
            id = entryId,
            title = "Mi gran día",
            content = "Contenido de detalle",
            mood = Mood.EUFORICA,
            category = Category.SOCIAL
        )

        val savedStateHandle = SavedStateHandle(mapOf("entryId" to entryId))
        coEvery { journalRepository.getEntryById(entryId) } returns mockEntry

        val viewModel = EntryDetailViewModel(savedStateHandle, journalRepository, deleteEntryUseCase, prepareShareContentUseCase)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertNotNull(loadedState.entry)
            assertEquals("Mi gran día", loadedState.entry?.title)
        }
    }
}
