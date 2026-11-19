package com.refugio.journal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.refugio.journal.data.local.dao.JournalDao
import com.refugio.journal.data.local.entity.JournalEntity
import com.refugio.journal.data.local.entity.PhotoEntity

@Database(
    entities = [JournalEntity::class, PhotoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RefugioDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN occurredAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE journal_entries SET occurredAt = createdAt WHERE occurredAt = 0")
            }
        }
    }
}
