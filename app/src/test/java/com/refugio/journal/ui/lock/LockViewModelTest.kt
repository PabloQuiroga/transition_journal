package com.refugio.journal.ui.lock

import app.cash.turbine.test
import com.refugio.journal.domain.repository.SecurityManager
import com.refugio.journal.domain.usecase.SetupPinUseCase
import com.refugio.journal.domain.usecase.VerifyAccessResult
import com.refugio.journal.domain.usecase.VerifyAccessUseCase
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
class LockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val securityManager: SecurityManager = mockk()
    private val verifyAccessUseCase: VerifyAccessUseCase = mockk()
    private val setupPinUseCase: SetupPinUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when PIN is not configured then uiState emits OnboardingPin`() = runTest {
        coEvery { securityManager.isPinConfigured() } returns false

        val viewModel = LockViewModel(securityManager, verifyAccessUseCase, setupPinUseCase)

        viewModel.uiState.test {
            assertEquals(LockUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(LockUiState.OnboardingPin, awaitItem())
        }
    }

    @Test
    fun `when PIN is configured then uiState emits Locked`() = runTest {
        coEvery { securityManager.isPinConfigured() } returns true

        val viewModel = LockViewModel(securityManager, verifyAccessUseCase, setupPinUseCase)

        viewModel.uiState.test {
            assertEquals(LockUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is LockUiState.Locked)
        }
    }

    @Test
    fun `when verifyPin succeeds then uiState emits Unlocked`() = runTest {
        coEvery { securityManager.isPinConfigured() } returns true
        coEvery { verifyAccessUseCase.verifyPin("1234") } returns VerifyAccessResult.Success

        val viewModel = LockViewModel(securityManager, verifyAccessUseCase, setupPinUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem() is LockUiState.Locked)
            viewModel.verifyPin("1234")
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(LockUiState.Unlocked, awaitItem())
        }
    }

    @Test
    fun `when lockSession is called then uiState transitions to Locked`() = runTest {
        coEvery { securityManager.isPinConfigured() } returns true

        val viewModel = LockViewModel(securityManager, verifyAccessUseCase, setupPinUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.lockSession()

        viewModel.uiState.test {
            assertTrue(awaitItem() is LockUiState.Locked)
        }
    }
}
