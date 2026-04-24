package com.patricklarocque.recallos.core.files

interface MemoryFileStore {
    suspend fun saveRawContent(
        memoryItemId: String,
        content: RawMemoryContent,
    ): StoredRawContent

    suspend fun deleteRawContent(path: String)
}
