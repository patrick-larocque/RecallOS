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
    @ColumnInfo(name = "original_file_name")
    val originalFileName: String?,
    @ColumnInfo(name = "mime_type")
    val mimeType: String?,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
    val sha256: String?,
    @ColumnInfo(name = "extracted_text")
    val extractedText: String?,
    @ColumnInfo(name = "processing_status")
    val processingStatus: String,
    @ColumnInfo(name = "sync_status")
    val syncStatus: String,
    @ColumnInfo(name = "failure_reason")
    val failureReason: String?,
    @ColumnInfo(name = "space_id")
    val spaceId: String?,
    @ColumnInfo(name = "source_uri")
    val sourceUri: String?,
    @ColumnInfo(name = "captured_at")
    val capturedAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
