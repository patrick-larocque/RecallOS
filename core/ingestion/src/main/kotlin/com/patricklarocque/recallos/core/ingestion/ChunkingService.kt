package com.patricklarocque.recallos.core.ingestion

data class TextChunk(
    val text: String,
    val chunkIndex: Int,
)

interface ChunkingService {
    fun chunk(text: String): List<TextChunk>
}
