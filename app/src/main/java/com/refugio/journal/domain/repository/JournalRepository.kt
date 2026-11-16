package com.refugio.journal.domain.repository

import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import kotlinx.coroutines.flow.Flow

data class EntryFilter(
    val category: Category? = null,
    val mood: Mood? = null,
    val isMilestoneOnly: Boolean = false,
    val queryText: String? = null
)

interface JournalRepository {
    fun observeEntries(filter: EntryFilter = EntryFilter()): Flow<List<JournalEntry>>
    suspend fun getEntryById(id: Long): JournalEntry?
    suspend fun saveEntry(entry: JournalEntry): Result<Long>
    suspend fun deleteEntry(id: Long): Result<Unit>
}
