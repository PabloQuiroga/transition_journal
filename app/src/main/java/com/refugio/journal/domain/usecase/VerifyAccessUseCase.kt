package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.repository.SecurityManager
import javax.inject.Inject
import javax.inject.Singleton

sealed interface VerifyAccessResult {
    data object Success : VerifyAccessResult
    data class InvalidPin(val remainingAttempts: Int) : VerifyAccessResult
    data class CooldownActive(val secondsRemaining: Long) : VerifyAccessResult
}

@Singleton
class VerifyAccessUseCase @Inject constructor(
    private val securityManager: SecurityManager
) {
    private var failedAttempts = 0
    private var cooldownEndTimeMillis = 0L

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val COOLDOWN_DURATION_SECONDS = 30L
    }

    suspend fun verifyPin(inputPin: String): VerifyAccessResult {
        val currentTimeMillis = System.currentTimeMillis()

        if (currentTimeMillis < cooldownEndTimeMillis) {
            val secondsRemaining = (cooldownEndTimeMillis - currentTimeMillis) / 1000 + 1
            return VerifyAccessResult.CooldownActive(secondsRemaining)
        }

        val isValid = securityManager.validatePin(inputPin)
        return if (isValid) {
            failedAttempts = 0
            VerifyAccessResult.Success
        } else {
            failedAttempts++
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                cooldownEndTimeMillis = currentTimeMillis + (COOLDOWN_DURATION_SECONDS * 1000)
                failedAttempts = 0
                VerifyAccessResult.CooldownActive(COOLDOWN_DURATION_SECONDS)
            } else {
                val remaining = MAX_FAILED_ATTEMPTS - failedAttempts
                VerifyAccessResult.InvalidPin(remainingAttempts = remaining)
            }
        }
    }
}
