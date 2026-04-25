package com.patricklarocque.recallos.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.patricklarocque.recallos.core.database.entity.MemoryChunkEntity
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity
import com.patricklarocque.recallos.core.database.entity.SpaceEntity
import com.patricklarocque.recallos.core.database.entity.VectorEmbeddingEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecallOsDatabaseTest {
    private lateinit var database: RecallOsDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RecallOsDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertsAndReadsMemoryItems() = runBlocking {
        val item = MemoryItemEntity(
            id = "memory-1",
            type = "SCREENSHOT",
            title = "Router settings",
            rawContentPath = "/memory/router-settings.png",
            originalFileName = "router-settings.png",
            mimeType = "image/png",
            sizeBytes = 128L,
            sha256 = "abc123",
            extractedText = "Wi-Fi password setup",
            processingStatus = "READY",
            syncStatus = "LOCAL_ONLY",
            failureReason = null,
            spaceId = null,
            sourceUri = null,
            capturedAt = 100L,
            createdAt = 100L,
            updatedAt = 150L,
        )

        database.memoryItemDao().upsert(item)

        val stored = database.memoryItemDao().getById("memory-1")
        assertThat(stored).isEqualTo(item)
    }

    @Test
    fun returnsChunksForMemoryItem() = runBlocking {
        database.memoryItemDao().upsert(
            memoryItemEntity(id = "memory-2", createdAt = 200L),
        )

        database.memoryChunkDao().upsert(
            listOf(
                MemoryChunkEntity(
                    id = "chunk-1",
                    memoryItemId = "memory-2",
                    text = "ADB over Wi-Fi on port 5555",
                    chunkIndex = 0,
                    embeddingRef = null,
                    createdAt = 220L,
                ),
            ),
        )

        val chunks = database.memoryChunkDao().getForMemoryItem("memory-2")
        assertThat(chunks).hasSize(1)
        assertThat(chunks.first().text).contains("ADB over Wi-Fi")
    }

    @Test
    fun returnsChunksInChunkIndexOrder() = runBlocking {
        database.memoryItemDao().upsert(memoryItemEntity(id = "memory-ordered", createdAt = 250L))
        database.memoryChunkDao().upsert(
            listOf(
                memoryChunkEntity(id = "chunk-2", memoryItemId = "memory-ordered", chunkIndex = 2),
                memoryChunkEntity(id = "chunk-0", memoryItemId = "memory-ordered", chunkIndex = 0),
                memoryChunkEntity(id = "chunk-1", memoryItemId = "memory-ordered", chunkIndex = 1),
            ),
        )

        val chunks = database.memoryChunkDao().getForMemoryItem("memory-ordered")

        assertThat(chunks.map { chunk -> chunk.chunkIndex }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun returnsMemoryItemsNewestFirst() = runBlocking {
        database.memoryItemDao().upsert(memoryItemEntity(id = "old", createdAt = 100L))
        database.memoryItemDao().upsert(memoryItemEntity(id = "new", createdAt = 300L))
        database.memoryItemDao().upsert(memoryItemEntity(id = "middle", createdAt = 200L))

        val items = database.memoryItemDao().getAll()

        assertThat(items.map { item -> item.id }).containsExactly("new", "middle", "old").inOrder()
    }

    @Test
    fun preservesOptionalSpaceAssociationOnMemoryItems() = runBlocking {
        database.spaceDao().upsert(
            SpaceEntity(
                id = "space-1",
                name = "Networking",
                createdAt = 300L,
            ),
        )

        database.memoryItemDao().upsert(
            memoryItemEntity(
                id = "memory-3",
                type = "FILE",
                title = "Switch manual",
                rawContentPath = "/memory/switch-manual.pdf",
                spaceId = "space-1",
                createdAt = 310L,
            ),
        )

        val stored = database.memoryItemDao().getById("memory-3")
        val space = database.spaceDao().getById("space-1")

        assertThat(stored?.spaceId).isEqualTo("space-1")
        assertThat(space?.name).isEqualTo("Networking")
    }

    @Test
    fun storesVectorEmbeddingForChunk() = runBlocking {
        database.memoryItemDao().upsert(memoryItemEntity(id = "memory-vector", createdAt = 400L))
        database.memoryChunkDao().upsert(
            listOf(memoryChunkEntity(id = "chunk-vector", memoryItemId = "memory-vector", chunkIndex = 0)),
        )
        val vector = floatArrayOf(0.25f, 0.5f, 0.75f)
        database.vectorEmbeddingDao().upsert(
            VectorEmbeddingEntity(
                chunkId = "chunk-vector",
                modelId = "test-model",
                dimensions = vector.size,
                vectorBlob = VectorBlobConverter.fromFloatArray(vector),
                createdAt = 410L,
            ),
        )

        val stored = database.vectorEmbeddingDao().getForChunk("chunk-vector")

        assertThat(stored?.modelId).isEqualTo("test-model")
        assertThat(stored?.dimensions).isEqualTo(3)
        assertThat(VectorBlobConverter.toFloatArray(stored!!.vectorBlob).toList())
            .containsExactly(0.25f, 0.5f, 0.75f)
            .inOrder()
    }

    @Test
    fun deletingMemoryItemCascadesChunksAndEmbeddings() = runBlocking {
        database.memoryItemDao().upsert(memoryItemEntity(id = "memory-delete", createdAt = 500L))
        database.memoryChunkDao().upsert(
            listOf(memoryChunkEntity(id = "chunk-delete", memoryItemId = "memory-delete", chunkIndex = 0)),
        )
        database.vectorEmbeddingDao().upsert(
            VectorEmbeddingEntity(
                chunkId = "chunk-delete",
                modelId = "test-model",
                dimensions = 1,
                vectorBlob = VectorBlobConverter.fromFloatArray(floatArrayOf(1f)),
                createdAt = 510L,
            ),
        )

        database.memoryItemDao().deleteById("memory-delete")

        assertThat(database.memoryChunkDao().getForMemoryItem("memory-delete")).isEmpty()
        assertThat(database.vectorEmbeddingDao().getForChunk("chunk-delete")).isNull()
    }

    private fun memoryItemEntity(
        id: String,
        type: String = "NOTE",
        title: String? = "Setup notes",
        rawContentPath: String? = "/memory/$id",
        extractedText: String? = "ADB over Wi-Fi",
        processingStatus: String = "READY",
        spaceId: String? = null,
        createdAt: Long,
    ): MemoryItemEntity {
        return MemoryItemEntity(
            id = id,
            type = type,
            title = title,
            rawContentPath = rawContentPath,
            originalFileName = "$id.txt",
            mimeType = "text/plain",
            sizeBytes = 42L,
            sha256 = "sha-$id",
            extractedText = extractedText,
            processingStatus = processingStatus,
            syncStatus = "LOCAL_ONLY",
            failureReason = null,
            spaceId = spaceId,
            sourceUri = null,
            capturedAt = createdAt,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
    }

    private fun memoryChunkEntity(
        id: String,
        memoryItemId: String,
        chunkIndex: Int,
    ): MemoryChunkEntity {
        return MemoryChunkEntity(
            id = id,
            memoryItemId = memoryItemId,
            text = "Chunk $chunkIndex",
            chunkIndex = chunkIndex,
            embeddingRef = null,
            createdAt = 1000L + chunkIndex,
        )
    }
}
