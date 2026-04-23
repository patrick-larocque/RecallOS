package com.patricklarocque.recallos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory_items",
    foreignKeys = [
        ForeignKey(
            entity = SpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["space_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["space_id"]),
        Index(value = ["type"]),
        Index(value = ["processing_status"]),
    ],
)
data class MemoryItemEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val title: String?,
    @ColumnInfo(name = "raw_content_path")
    val rawContentPath: String?,
    @ColumnInfo(name = "extracted_text")
    val extractedText: String?,
    @ColumnInfo(name = "processing_status")
    val processingStatus: String,
    @ColumnInfo(name = "space_id")
    val spaceId: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
