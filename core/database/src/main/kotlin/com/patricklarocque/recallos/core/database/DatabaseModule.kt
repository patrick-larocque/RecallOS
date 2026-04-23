package com.patricklarocque.recallos.core.database

import android.content.Context
import androidx.room.Room
import com.patricklarocque.recallos.core.database.dao.MemoryChunkDao
import com.patricklarocque.recallos.core.database.dao.MemoryItemDao
import com.patricklarocque.recallos.core.database.dao.SpaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DatabaseName = "recallos.db"

    @Provides
    @Singleton
    fun provideRecallOsDatabase(
        @ApplicationContext context: Context,
    ): RecallOsDatabase {
        return Room.databaseBuilder(
            context,
            RecallOsDatabase::class.java,
            DatabaseName,
        )
            .build()
    }

    @Provides
    fun provideMemoryItemDao(database: RecallOsDatabase): MemoryItemDao = database.memoryItemDao()

    @Provides
    fun provideMemoryChunkDao(database: RecallOsDatabase): MemoryChunkDao = database.memoryChunkDao()

    @Provides
    fun provideSpaceDao(database: RecallOsDatabase): SpaceDao = database.spaceDao()
}
