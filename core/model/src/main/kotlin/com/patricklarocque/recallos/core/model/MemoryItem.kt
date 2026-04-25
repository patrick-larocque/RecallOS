package com.patricklarocque.recallos.core.model

data class MemoryItem(
    val id: String,
    val type: MemoryType,
    val title: String?,
    val rawContentPath: String?,
    val originalFileName: String?,
    val mimeType: String?,
    val sizeBytes: Long,
    val sha256: String?,
    val extractedText: String?,
    val processingStatus: ProcessingStatus,
    val syncStatus: SyncStatus,
    val failureReason: String?,
    val spaceId: String?,
    val sourceUri: String?,
    val capturedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
)
