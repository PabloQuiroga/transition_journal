package com.refugio.journal.core.di

import android.content.Context
import androidx.room.Room
import com.refugio.journal.data.local.dao.JournalDao
import com.refugio.journal.data.local.database.RefugioDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRefugioDatabase(
        @ApplicationContext context: Context
    ): RefugioDatabase {
        return Room.databaseBuilder(
            context,
            RefugioDatabase::class.java,
            "refugio_journal.db"
        ).addMigrations(RefugioDatabase.MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideJournalDao(
        database: RefugioDatabase
    ): JournalDao {
        return database.journalDao()
    }
}
