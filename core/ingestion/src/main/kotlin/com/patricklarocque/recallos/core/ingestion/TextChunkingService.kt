package com.patricklarocque.recallos.core.ingestion

import javax.inject.Inject

class TextChunkingService @Inject constructor() : ChunkingService {

    private val chunkSize = 512
    private val overlap = 128

    override fun chunk(text: String): List<TextChunk> {
        val normalized = text.trim()
        if (normalized.isBlank()) return emptyList()
        if (normalized.length <= chunkSize) return listOf(TextChunk(normalized, 0))

        val chunks = mutableListOf<TextChunk>()
        var start = 0
        var index = 0
        while (start < normalized.length) {
            val rawEnd = minOf(start + chunkSize, normalized.length)
            val end = if (rawEnd < normalized.length) {
                val boundary = normalized.lastIndexOf(' ', rawEnd)
                if (boundary > start) boundary else rawEnd
            } else {
                rawEnd
            }
            chunks += TextChunk(normalized.substring(start, end).trim(), index++)
            val nextStart = end - overlap
            start = if (nextStart > start) nextStart else start + 1
        }
        return chunks
    }
}
