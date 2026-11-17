package com.refugio.journal.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(
    viewModel: LockViewModel,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LockUiState.Unlocked) {
            onUnlocked()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is LockUiState.Loading -> LoadingStateContent()
            is LockUiState.OnboardingPin -> OnboardingPinContent(
                onPinCompleted = { rawPin, confirmPin -> viewModel.setupPin(rawPin, confirmPin) }
            )
            is LockUiState.Locked -> LockedContent(
                state = state,
                onPinSubmitted = { pin -> viewModel.verifyPin(pin) }
            )
            is LockUiState.Unlocked -> UnlockedStateContent()
        }
    }
}

@Composable
private fun LoadingStateContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun UnlockedStateContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Sesión Desbloqueada",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
private fun OnboardingPinContent(
    onPinCompleted: (String, String) -> Unit
) {
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentInput = if (isConfirming) confirmPin else firstPin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LockHeader(
                icon = Icons.Default.Lock,
                title = if (isConfirming) "Confirma tu PIN" else "Crea tu PIN de seguridad",
                subtitle = "Ingresa entre 4 y 6 dígitos numéricos"
            )
            Spacer(modifier = Modifier.height(24.dp))
            PinDotsIndicator(pinLength = currentInput.length)

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        NumericKeypad(
            onNumberClick = { number ->
                if (currentInput.length < 6) {
                    if (isConfirming) confirmPin += number else firstPin += number
                }
            },
            onDeleteClick = {
                if (isConfirming) {
                    if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                } else {
                    if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
                }
            },
            onConfirmClick = {
                if (currentInput.length >= 4) {
                    if (!isConfirming) {
                        isConfirming = true
                        errorMessage = null
                    } else {
                        if (firstPin == confirmPin) {
                            onPinCompleted(firstPin, confirmPin)
                        } else {
                            errorMessage = "Los PINs no coinciden. Inténtalo de nuevo."
                            confirmPin = ""
                            firstPin = ""
                            isConfirming = false
                        }
                    }
                } else {
                    errorMessage = "El PIN debe tener al menos 4 dígitos"
                }
            }
        )
    }
}

@Composable
private fun LockedContent(
    state: LockUiState.Locked,
    onPinSubmitted: (String) -> Unit
) {
    var inputPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LockHeader(
                icon = Icons.Default.Lock,
                title = "Refugio",
                subtitle = "Bóveda de Privacidad"
            )
            Spacer(modifier = Modifier.height(24.dp))
            PinDotsIndicator(pinLength = inputPin.length)

            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
            state.remainingAttempts?.let { attempts ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Intentos restantes: $attempts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.cooldownSecondsRemaining?.let { seconds ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bloqueado por $seconds segundos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        NumericKeypad(
            onNumberClick = { number ->
                if (inputPin.length < 6) inputPin += number
            },
            onDeleteClick = {
                if (inputPin.isNotEmpty()) inputPin = inputPin.dropLast(1)
            },
            onConfirmClick = {
                if (inputPin.length >= 4) {
                    onPinSubmitted(inputPin)
                    inputPin = ""
                }
            }
        )
    }
}

@Composable
private fun LockHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(top = 40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PinDotsIndicator(
    pinLength: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val isFilled = index < pinLength
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("del", "0", "ok")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { item ->
                    when (item) {
                        "del" -> KeypadIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Borrar",
                            onClick = onDeleteClick
                        )
                        "ok" -> KeypadActionButton(
                            text = "OK",
                            onClick = onConfirmClick
                        )
                        else -> KeypadNumberButton(
                            number = item,
                            onClick = { onNumberClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadNumberButton(
    number: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        shape = CircleShape
    ) {
        Text(
            text = number,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun KeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun KeypadActionButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
