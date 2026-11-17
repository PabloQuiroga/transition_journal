package com.refugio.journal.data.repository

import com.refugio.journal.data.local.dao.JournalDao
import com.refugio.journal.data.local.entity.JournalEntity
import com.refugio.journal.data.local.entity.PhotoEntity
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.repository.EntryFilter
import com.refugio.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao
) : JournalRepository {

    override fun observeEntries(filter: EntryFilter): Flow<List<JournalEntry>> {
        return journalDao.observeAllEntries().map { entities ->
            val entryIds = entities.map { it.id }
            val photos = if (entryIds.isNotEmpty()) {
                journalDao.getPhotosForEntries(entryIds)
            } else {
                emptyList()
            }
            val photosByEntryId = photos.groupBy { it.entryId }

            entities.map { entity ->
                val entryPhotos = photosByEntryId[entity.id]?.map { it.toDomainModel() } ?: emptyList()
                entity.toDomainModel(entryPhotos)
            }.filter { entry ->
                matchesFilter(entry, filter)
            }
        }
    }

    override suspend fun getEntryById(id: Long): JournalEntry? {
        val entity = journalDao.getEntryById(id) ?: return null
        val photos = journalDao.getPhotosForEntry(id).map { it.toDomainModel() }
        return entity.toDomainModel(photos)
    }

    override suspend fun saveEntry(entry: JournalEntry): Result<Long> {
        return runCatching {
            val entity = JournalEntity.fromDomainModel(entry)
            val savedId = journalDao.insertEntry(entity)

            if (entry.photos.isNotEmpty()) {
                val photoEntities = entry.photos.map { photo ->
                    PhotoEntity.fromDomainModel(photo, savedId)
                }
                journalDao.insertPhotos(photoEntities)
            }

            savedId
        }
    }

    override suspend fun deleteEntry(id: Long): Result<Unit> {
        return runCatching {
            journalDao.deleteEntryById(id)
        }
    }

    private fun matchesFilter(entry: JournalEntry, filter: EntryFilter): Boolean {
        if (filter.category != null && entry.category != filter.category) return false
        if (filter.mood != null && entry.mood != filter.mood) return false
        if (filter.isMilestoneOnly && !entry.isMilestone) return false
        if (!filter.queryText.isNullOrBlank()) {
            val query = filter.queryText.trim().lowercase()
            val matchesTitle = entry.title.lowercase().contains(query)
            val matchesContent = entry.content.lowercase().contains(query)
            if (!matchesTitle && !matchesContent) return false
        }
        return true
    }
}
