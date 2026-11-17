package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.repository.SecurityManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifyAccessUseCaseTest {

    private val securityManager: SecurityManager = mockk()
    private val useCase = VerifyAccessUseCase(securityManager)

    @Test
    fun `when pin is correct then returns success result`() = runTest {
        coEvery { securityManager.validatePin("1234") } returns true

        val result = useCase.verifyPin("1234")

        assertTrue(result is VerifyAccessResult.Success)
    }

    @Test
    fun `when pin is incorrect then returns invalid pin with remaining attempts`() = runTest {
        coEvery { securityManager.validatePin("0000") } returns false

        val result = useCase.verifyPin("0000")

        assertTrue(result is VerifyAccessResult.InvalidPin)
        val invalidPinResult = result as VerifyAccessResult.InvalidPin
        assertEquals(4, invalidPinResult.remainingAttempts)
    }

    @Test
    fun `when 5 consecutive failed attempts occur then triggers cooldown`() = runTest {
        coEvery { securityManager.validatePin("wrong") } returns false

        repeat(4) {
            useCase.verifyPin("wrong")
        }

        val result = useCase.verifyPin("wrong")

        assertTrue(result is VerifyAccessResult.CooldownActive)
    }
}
