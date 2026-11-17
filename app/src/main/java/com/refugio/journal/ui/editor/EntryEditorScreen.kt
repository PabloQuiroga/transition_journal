package com.refugio.journal.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.Mood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorScreen(
    viewModel: EntryEditorViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onSaveSuccess()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Nueva Entrada", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveEntry() },
                        enabled = !uiState.isSaving && (uiState.content.isNotBlank() || uiState.photos.isNotEmpty())
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Título (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("¿Cómo fue tu día? *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Mood Selector
            Text(text = "Estado Emocional", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Mood.entries) { mood ->
                    FilterChip(
                        selected = uiState.mood == mood,
                        onClick = { viewModel.setMood(mood) },
                        label = { Text(getMoodDisplayName(mood)) }
                    )
                }
            }

            // Category Selector
            Text(text = "Categoría", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Category.entries) { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(getCategoryDisplayName(category)) }
                    )
                }
            }

            // Milestone Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Marcar como Hito", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Destacar este momento como clave",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = uiState.isMilestone,
                    onCheckedChange = { viewModel.setMilestone(it) }
                )
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun getMoodDisplayName(mood: Mood): String {
    return when (mood) {
        Mood.EUFORICA -> "✨ Eufórica"
        Mood.EMPODERADA -> "💪 Empoderada"
        Mood.TRANQUILA -> "🌿 Tranquila"
        Mood.VULNERABLE -> "💧 Vulnerable"
        Mood.ANSIOSA -> "⚡ Ansiosa"
    }
}

private fun getCategoryDisplayName(category: Category): String {
    return when (category) {
        Category.EXPRESION_ESTILO -> "👗 Expresión y Estilo"
        Category.SOCIAL -> "💬 Social"
        Category.EMOCIONAL -> "🧠 Emocional"
        Category.HITO -> "⭐ Hito"
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Entry Editor Screen", showBackground = true)
@Composable
private fun EntryEditorScreenPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = "Mi primer día con pronombre elle",
                    onValueChange = {},
                    label = { Text("Título (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = "Hoy tuve una charla muy cálida con mis amigues...",
                    onValueChange = {},
                    label = { Text("¿Cómo fue tu día? *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Text(text = "Estado Emocional", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Mood.entries) { mood ->
                        FilterChip(
                            selected = mood == Mood.EUFORICA,
                            onClick = {},
                            label = { Text(getMoodDisplayName(mood)) }
                        )
                    }
                }
            }
        }
    }
}
