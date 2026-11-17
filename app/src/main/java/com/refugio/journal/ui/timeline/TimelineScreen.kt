package com.refugio.journal.ui.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    onNewEntryClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Mi Diario de Transición", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEntryClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva entrada")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterChipsRow(
                selectedCategory = uiState.filter.category,
                isMilestoneOnly = uiState.filter.isMilestoneOnly,
                onCategorySelected = { category -> viewModel.setCategory(category) },
                onMilestoneToggle = { isMilestone -> viewModel.setMilestoneOnly(isMilestone) }
            )

            if (uiState.isLoading && uiState.entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay entradas registradas aún.\n¡Presiona + para crear una!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedCategory: Category?,
    isMilestoneOnly: Boolean,
    onCategorySelected: (Category?) -> Unit,
    onMilestoneToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null && !isMilestoneOnly,
                onClick = {
                    onCategorySelected(null)
                    onMilestoneToggle(false)
                },
                label = { Text("Todas") }
            )
        }
        item {
            FilterChip(
                selected = isMilestoneOnly,
                onClick = { onMilestoneToggle(!isMilestoneOnly) },
                label = { Text("⭐ Hitos") }
            )
        }
        Category.entries.forEach { category ->
            item {
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(if (selectedCategory == category) null else category)
                    },
                    label = { Text(getCategoryDisplayName(category)) }
                )
            }
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        .withZone(ZoneId.systemDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (entry.isMilestone) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Hito",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormatter.format(entry.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = getMoodDisplayName(entry.mood),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = getCategoryDisplayName(entry.category),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
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

@Preview(name = "Timeline Loaded", showBackground = true)
@Composable
private fun TimelineScreenPreview() {
    MaterialTheme {
        Surface {
            val sampleEntries = listOf(
                JournalEntry(
                    id = 1L,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    title = "Primera salida con nombre elegido",
                    content = "Fue una experiencia increíblemente liberadora y hermosa.",
                    mood = Mood.EUFORICA,
                    category = Category.SOCIAL,
                    isMilestone = true
                ),
                JournalEntry(
                    id = 2L,
                    createdAt = Instant.now().minusSeconds(86400),
                    updatedAt = Instant.now().minusSeconds(86400),
                    title = "Probando nuevo estilo",
                    content = "Me siento muy cómoda con este outfit.",
                    mood = Mood.TRANQUILA,
                    category = Category.EXPRESION_ESTILO,
                    isMilestone = false
                )
            )
            Column(modifier = Modifier.fillMaxSize()) {
                FilterChipsRow(
                    selectedCategory = null,
                    isMilestoneOnly = false,
                    onCategorySelected = {},
                    onMilestoneToggle = {}
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sampleEntries) { entry ->
                        JournalEntryCard(entry = entry, onClick = {})
                    }
                }
            }
        }
    }
}
