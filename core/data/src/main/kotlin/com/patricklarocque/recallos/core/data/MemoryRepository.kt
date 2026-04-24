package com.patricklarocque.recallos.core.data

import com.patricklarocque.recallos.core.model.MemoryItem

interface MemoryRepository {
    suspend fun saveRawMemory(input: NewMemoryInput): MemoryItem
}
