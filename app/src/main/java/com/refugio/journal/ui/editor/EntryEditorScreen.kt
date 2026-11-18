package com.refugio.journal.ui.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        uris.forEach { uri ->
            viewModel.importPhoto(uri.toString())
        }
    }

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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Adjuntar foto")
            }
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
                    .height(150.dp)
            )

            // Photos Carousel
            if (uiState.photos.isNotEmpty()) {
                Text(text = "Fotografías adjuntas (${uiState.photos.size}/5)", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.photos, key = { it.fileName }) { photo ->
                        Box(modifier = Modifier.size(90.dp)) {
                            AsyncImage(
                                model = photo.absolutePath,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.removePhoto(photo.fileName) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar foto",
                                        modifier = Modifier.padding(4.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

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

@Preview(name = "Entry Editor Screen with Photos", showBackground = true)
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
                        .height(150.dp)
                )

                Text(text = "Fotografías adjuntas (1/5)", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Box(modifier = Modifier.size(90.dp)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📷 Foto", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

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
