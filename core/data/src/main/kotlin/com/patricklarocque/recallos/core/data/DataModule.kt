package com.patricklarocque.recallos.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindMemoryRepository(
        repository: OfflineFirstMemoryRepository,
    ): MemoryRepository
}
