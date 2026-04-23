package com.patricklarocque.recallos.core.model

data class MemoryChunk(
    val id: String,
    val memoryItemId: String,
    val text: String,
    val chunkIndex: Int,
    val embeddingRef: String?,
    val createdAt: Long,
)
