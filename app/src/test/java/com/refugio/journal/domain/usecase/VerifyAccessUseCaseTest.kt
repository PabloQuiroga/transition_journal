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

    @Test
    fun `when entering correct pin after failed attempts then resets failed attempts counter`() = runTest {
        coEvery { securityManager.validatePin("wrong") } returns false
        coEvery { securityManager.validatePin("1234") } returns true

        repeat(3) {
            useCase.verifyPin("wrong")
        }

        val successResult = useCase.verifyPin("1234")
        assertTrue(successResult is VerifyAccessResult.Success)

        val nextFailedResult = useCase.verifyPin("wrong")
        assertTrue(nextFailedResult is VerifyAccessResult.InvalidPin)
        assertEquals(4, (nextFailedResult as VerifyAccessResult.InvalidPin).remainingAttempts)
    }

    @Test
    fun `when verifyPin is called during active cooldown then returns CooldownActive immediately`() = runTest {
        coEvery { securityManager.validatePin("wrong") } returns false

        repeat(5) {
            useCase.verifyPin("wrong")
        }

        val cooldownResult = useCase.verifyPin("1234")

        assertTrue(cooldownResult is VerifyAccessResult.CooldownActive)
    }
}
