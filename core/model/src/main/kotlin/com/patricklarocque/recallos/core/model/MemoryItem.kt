package com.patricklarocque.recallos.core.model

data class MemoryItem(
    val id: String,
    val type: MemoryType,
    val title: String?,
    val rawContentPath: String?,
    val extractedText: String?,
    val processingStatus: ProcessingStatus,
    val spaceId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
