package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.repository.SecurityManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupPinUseCaseTest {

    private val securityManager: SecurityManager = mockk()
    private val useCase = SetupPinUseCase(securityManager)

    @Test
    fun `when pin length is less than 4 digits then returns failure`() = runTest {
        val result = useCase(rawPin = "123", confirmPin = "123")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when pin contains non-numeric characters then returns failure`() = runTest {
        val result = useCase(rawPin = "123a", confirmPin = "123a")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when pin and confirmation do not match then returns failure`() = runTest {
        val result = useCase(rawPin = "1234", confirmPin = "1235")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when pin is valid and matches confirmation then saves pin via securityManager`() = runTest {
        coEvery { securityManager.setupPin("1234") } returns Result.success(Unit)

        val result = useCase(rawPin = "1234", confirmPin = "1234")

        assertTrue(result.isSuccess)
        coVerify { securityManager.setupPin("1234") }
    }
}
