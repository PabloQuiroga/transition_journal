package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.repository.SecurityManager
import javax.inject.Inject

class SetupPinUseCase @Inject constructor(
    private val securityManager: SecurityManager
) {
    suspend operator fun invoke(rawPin: String, confirmPin: String): Result<Unit> {
        if (rawPin != confirmPin) {
            return Result.failure(IllegalArgumentException("PIN confirmation does not match"))
        }

        if (rawPin.length !in 4..6 || !rawPin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("PIN must be 4 to 6 numeric digits"))
        }

        return securityManager.setupPin(rawPin)
    }
}
