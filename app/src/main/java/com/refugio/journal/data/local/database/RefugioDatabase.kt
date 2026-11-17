package com.refugio.journal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.refugio.journal.data.local.dao.JournalDao
import com.refugio.journal.data.local.entity.JournalEntity
import com.refugio.journal.data.local.entity.PhotoEntity

@Database(
    entities = [JournalEntity::class, PhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RefugioDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
}
