package com.refugio.journal.ui.editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.Mood
import com.refugio.journal.domain.repository.JournalRepository
import com.refugio.journal.domain.usecase.CreateOrUpdateEntryUseCase
import com.refugio.journal.domain.usecase.ImportPhotoUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val savedStateHandle = SavedStateHandle()
    private val journalRepository: JournalRepository = mockk(relaxed = true)
    private val createOrUpdateEntryUseCase: CreateOrUpdateEntryUseCase = mockk()
    private val importPhotoUseCase: ImportPhotoUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when updating fields then uiState reflects changes`() = runTest {
        val viewModel = EntryEditorViewModel(savedStateHandle, journalRepository, createOrUpdateEntryUseCase, importPhotoUseCase)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals("", initialState.title)

            viewModel.updateTitle("Mi nuevo día")
            val updatedState = awaitItem()
            assertEquals("Mi nuevo día", updatedState.title)
        }
    }

    @Test
    fun `when saving valid entry then emits isSavedSuccess true`() = runTest {
        val viewModel = EntryEditorViewModel(savedStateHandle, journalRepository, createOrUpdateEntryUseCase, importPhotoUseCase)
        viewModel.updateContent("Nota importante sobre mi transición")
        viewModel.setMood(Mood.EMPODERADA)
        viewModel.setCategory(Category.SOCIAL)

        coEvery { createOrUpdateEntryUseCase(any()) } returns Result.success(1L)

        viewModel.uiState.test {
            assertEquals(false, awaitItem().isSavedSuccess)

            viewModel.saveEntry()
            testDispatcher.scheduler.advanceUntilIdle()

            val savingState = awaitItem()
            assertTrue(savingState.isSaving)

            val savedState = awaitItem()
            assertTrue(savedState.isSavedSuccess)
        }
    }
}
