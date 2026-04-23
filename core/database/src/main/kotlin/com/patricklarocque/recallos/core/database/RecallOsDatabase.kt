package com.patricklarocque.recallos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.patricklarocque.recallos.core.database.dao.MemoryChunkDao
import com.patricklarocque.recallos.core.database.dao.MemoryItemDao
import com.patricklarocque.recallos.core.database.dao.SpaceDao
import com.patricklarocque.recallos.core.database.entity.MemoryChunkEntity
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity
import com.patricklarocque.recallos.core.database.entity.SpaceEntity

@Database(
    entities = [
        MemoryItemEntity::class,
        MemoryChunkEntity::class,
        SpaceEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RecallOsDatabase : RoomDatabase() {
    abstract fun memoryItemDao(): MemoryItemDao
    abstract fun memoryChunkDao(): MemoryChunkDao
    abstract fun spaceDao(): SpaceDao
}
