package com.refugio.journal.core.di

import com.refugio.journal.core.security.AndroidSecurityManager
import com.refugio.journal.core.storage.PrivatePhotoStorageManager
import com.refugio.journal.data.repository.JournalRepositoryImpl
import com.refugio.journal.domain.repository.JournalRepository
import com.refugio.journal.domain.repository.PhotoStorageManager
import com.refugio.journal.domain.repository.SecurityManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        impl: JournalRepositoryImpl
    ): JournalRepository

    @Binds
    @Singleton
    abstract fun bindPhotoStorageManager(
        impl: PrivatePhotoStorageManager
    ): PhotoStorageManager

    @Binds
    @Singleton
    abstract fun bindSecurityManager(
        impl: AndroidSecurityManager
    ): SecurityManager
}
