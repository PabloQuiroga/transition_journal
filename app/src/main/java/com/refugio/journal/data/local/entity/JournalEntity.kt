package com.refugio.journal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.refugio.journal.domain.model.Category
import com.refugio.journal.domain.model.EntryPhoto
import com.refugio.journal.domain.model.JournalEntry
import com.refugio.journal.domain.model.Mood
import java.time.Instant

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val createdAt: Long,
    val updatedAt: Long,
    val title: String,
    val content: String,
    val mood: String,
    val category: String,
    val isMilestone: Boolean
) {
    fun toDomainModel(photos: List<EntryPhoto> = emptyList()): JournalEntry {
        return JournalEntry(
            id = id,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt),
            title = title,
            content = content,
            mood = Mood.valueOf(mood),
            category = Category.valueOf(category),
            isMilestone = isMilestone,
            photos = photos
        )
    }

    companion object {
        fun fromDomainModel(entry: JournalEntry): JournalEntity {
            return JournalEntity(
                id = entry.id,
                createdAt = entry.createdAt.toEpochMilli(),
                updatedAt = entry.updatedAt.toEpochMilli(),
                title = entry.title,
                content = entry.content,
                mood = entry.mood.name,
                category = entry.category.name,
                isMilestone = entry.isMilestone
            )
        }
    }
}
