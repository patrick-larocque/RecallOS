package com.patricklarocque.recallos.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity

@Dao
interface MemoryItemDao {
    @Upsert
    suspend fun upsert(item: MemoryItemEntity)

    @Query("SELECT * FROM memory_items WHERE id = :id")
    suspend fun getById(id: String): MemoryItemEntity?

    @Query("SELECT * FROM memory_items ORDER BY created_at DESC")
    suspend fun getAll(): List<MemoryItemEntity>
}
