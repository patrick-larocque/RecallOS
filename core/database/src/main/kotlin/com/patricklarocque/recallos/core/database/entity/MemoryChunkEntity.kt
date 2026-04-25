package com.patricklarocque.recallos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory_chunks",
    foreignKeys = [
        ForeignKey(
            entity = MemoryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["memory_item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["memory_item_id"]),
    ],
)
data class MemoryChunkEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "memory_item_id")
    val memoryItemId: String,
    val text: String,
    @ColumnInfo(name = "chunk_index")
    val chunkIndex: Int,
    @ColumnInfo(name = "embedding_ref")
    val embeddingRef: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
