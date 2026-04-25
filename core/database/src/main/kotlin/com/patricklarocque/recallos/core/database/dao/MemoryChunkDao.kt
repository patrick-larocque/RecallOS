package com.patricklarocque.recallos.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.patricklarocque.recallos.core.database.entity.MemoryChunkEntity

@Dao
interface MemoryChunkDao {
    @Upsert
    suspend fun upsert(chunks: List<MemoryChunkEntity>)

    @Query(
        """
        SELECT * FROM memory_chunks
        WHERE memory_item_id = :memoryItemId
        ORDER BY chunk_index ASC
        """,
    )
    suspend fun getForMemoryItem(memoryItemId: String): List<MemoryChunkEntity>
}
