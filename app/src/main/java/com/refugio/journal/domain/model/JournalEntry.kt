package com.refugio.journal.domain.model

import java.time.Instant

data class JournalEntry(
    val id: Long = 0L,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val title: String,
    val content: String,
    val mood: Mood,
    val category: Category,
    val isMilestone: Boolean = false,
    val photos: List<EntryPhoto> = emptyList()
)
