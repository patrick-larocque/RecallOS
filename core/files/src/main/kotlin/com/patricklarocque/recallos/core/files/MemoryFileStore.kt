package com.patricklarocque.recallos.core.files

import java.io.InputStream

interface MemoryFileStore {
    suspend fun saveRawContent(
        memoryItemId: String,
        content: RawMemoryContent,
    ): StoredRawContent

    suspend fun saveRawContent(
        memoryItemId: String,
        originalFileName: String?,
        mimeType: String?,
        inputStream: InputStream,
    ): StoredRawContent

    suspend fun openRawContent(path: String): InputStream

    suspend fun deleteRawContent(path: String)
}
