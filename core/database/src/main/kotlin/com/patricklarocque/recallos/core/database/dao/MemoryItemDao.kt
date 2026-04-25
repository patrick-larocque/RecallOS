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

    @Query("SELECT * FROM memory_items ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<MemoryItemEntity>

    @Query(
        """
        UPDATE memory_items
        SET processing_status = :status,
            failure_reason = :failureReason,
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateProcessingStatus(
        id: String,
        status: String,
        failureReason: String?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE memory_items
        SET extracted_text = :extractedText,
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateExtractedText(
        id: String,
        extractedText: String?,
        updatedAt: Long,
    )

    @Query("DELETE FROM memory_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
