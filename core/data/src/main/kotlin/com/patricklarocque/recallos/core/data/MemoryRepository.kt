package com.patricklarocque.recallos.core.data

import com.patricklarocque.recallos.core.model.MemoryItem
import com.patricklarocque.recallos.core.model.ProcessingStatus

interface MemoryRepository {
    suspend fun saveRawMemory(input: NewMemoryInput): MemoryItem

    suspend fun getMemory(id: String): MemoryItem?

    suspend fun getRecentMemories(limit: Int): List<MemoryItem>

    suspend fun updateProcessingStatus(
        id: String,
        status: ProcessingStatus,
        failureReason: String? = null,
    ): MemoryItem?

    suspend fun updateExtractedText(
        id: String,
        extractedText: String?,
    ): MemoryItem?

    suspend fun deleteMemory(id: String)
}
