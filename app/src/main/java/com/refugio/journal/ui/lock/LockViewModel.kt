package com.refugio.journal.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.journal.domain.repository.SecurityManager
import com.refugio.journal.domain.usecase.SetupPinUseCase
import com.refugio.journal.domain.usecase.VerifyAccessResult
import com.refugio.journal.domain.usecase.VerifyAccessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LockUiState {
    data object Loading : LockUiState
    data object OnboardingPin : LockUiState
    data class Locked(
        val remainingAttempts: Int? = null,
        val cooldownSecondsRemaining: Long? = null,
        val errorMessage: String? = null
    ) : LockUiState
    data object Unlocked : LockUiState
}

@HiltViewModel
class LockViewModel @Inject constructor(
    private val securityManager: SecurityManager,
    private val verifyAccessUseCase: VerifyAccessUseCase,
    private val setupPinUseCase: SetupPinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LockUiState>(LockUiState.Loading)
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        checkSecurityState()
    }

    fun checkSecurityState() {
        viewModelScope.launch {
            _uiState.value = LockUiState.Loading
            val isConfigured = securityManager.isPinConfigured()
            _uiState.value = if (isConfigured) {
                LockUiState.Locked()
            } else {
                LockUiState.OnboardingPin
            }
        }
    }

    fun verifyPin(inputPin: String) {
        viewModelScope.launch {
            when (val result = verifyAccessUseCase.verifyPin(inputPin)) {
                is VerifyAccessResult.Success -> {
                    _uiState.value = LockUiState.Unlocked
                }
                is VerifyAccessResult.InvalidPin -> {
                    _uiState.value = LockUiState.Locked(
                        remainingAttempts = result.remainingAttempts,
                        errorMessage = "PIN incorrecto"
                    )
                }
                is VerifyAccessResult.CooldownActive -> {
                    _uiState.value = LockUiState.Locked(
                        cooldownSecondsRemaining = result.secondsRemaining,
                        errorMessage = "Demasiados intentos fallidos"
                    )
                }
            }
        }
    }

    fun setupPin(rawPin: String, confirmPin: String) {
        viewModelScope.launch {
            val result = setupPinUseCase(rawPin, confirmPin)
            if (result.isSuccess) {
                _uiState.value = LockUiState.Unlocked
            } else {
                _uiState.value = LockUiState.OnboardingPin
            }
        }
    }

    fun lockSession() {
        viewModelScope.launch {
            if (securityManager.isPinConfigured()) {
                _uiState.value = LockUiState.Locked()
            }
        }
    }
}
