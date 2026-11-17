package com.refugio.journal.domain.repository

interface SecurityManager {
    suspend fun isPinConfigured(): Boolean
    suspend fun setupPin(rawPin: String): Result<Unit>
    suspend fun validatePin(inputPin: String): Boolean
    fun isBiometricAvailable(): Boolean
}
