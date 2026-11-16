package com.refugio.journal.domain.usecase

import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.repository.EntryFilter
import com.refugio.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilteredTimelineUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    operator fun invoke(filter: EntryFilter = EntryFilter()): Flow<List<JournalEntry>> {
        return repository.observeEntries(filter)
    }
}
