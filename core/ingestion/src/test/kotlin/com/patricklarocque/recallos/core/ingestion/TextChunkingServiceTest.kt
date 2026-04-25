package com.patricklarocque.recallos.core.ingestion

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class TextChunkingServiceTest {

    private lateinit var service: TextChunkingService

    @Before
    fun setUp() {
        service = TextChunkingService()
    }

    @Test
    fun emptyText_returnsEmptyList() {
        assertThat(service.chunk("")).isEmpty()
        assertThat(service.chunk("   ")).isEmpty()
    }

    @Test
    fun shortText_returnsSingleChunk() {
        val text = "Hello world"
        val chunks = service.chunk(text)

        assertThat(chunks).hasSize(1)
        assertThat(chunks[0].text).isEqualTo(text)
        assertThat(chunks[0].chunkIndex).isEqualTo(0)
    }

    @Test
    fun textExactlyAtChunkSize_returnsSingleChunk() {
        val text = "a".repeat(512)
        val chunks = service.chunk(text)

        assertThat(chunks).hasSize(1)
    }

    @Test
    fun longText_returnsMultipleChunks() {
        val text = "word ".repeat(200)
        val chunks = service.chunk(text)

        assertThat(chunks.size).isGreaterThan(1)
    }

    @Test
    fun chunks_haveSequentialIndices() {
        val text = "word ".repeat(200)
        val chunks = service.chunk(text)

        chunks.forEachIndexed { index, chunk ->
            assertThat(chunk.chunkIndex).isEqualTo(index)
        }
    }

    @Test
    fun chunks_coverFullText() {
        val words = (1..150).map { "word$it" }
        val text = words.joinToString(" ")
        val chunks = service.chunk(text)

        val allText = chunks.joinToString(" ") { it.text }
        words.forEach { word ->
            assertThat(allText).contains(word)
        }
    }
}
