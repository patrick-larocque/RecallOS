package com.patricklarocque.recallos.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.patricklarocque.recallos.core.database.entity.MemoryChunkEntity
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity
import com.patricklarocque.recallos.core.database.entity.SpaceEntity
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
            extractedText = "Wi-Fi password setup",
            processingStatus = "READY",
            spaceId = null,
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
            MemoryItemEntity(
                id = "memory-2",
                type = "NOTE",
                title = "Setup notes",
                rawContentPath = null,
                extractedText = "ADB over Wi-Fi",
                processingStatus = "READY",
                spaceId = null,
                createdAt = 200L,
                updatedAt = 210L,
            ),
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
    fun preservesOptionalSpaceAssociationOnMemoryItems() = runBlocking {
        database.spaceDao().upsert(
            SpaceEntity(
                id = "space-1",
                name = "Networking",
                createdAt = 300L,
            ),
        )

        database.memoryItemDao().upsert(
            MemoryItemEntity(
                id = "memory-3",
                type = "FILE",
                title = "Switch manual",
                rawContentPath = "/memory/switch-manual.pdf",
                extractedText = null,
                processingStatus = "PENDING",
                spaceId = "space-1",
                createdAt = 310L,
                updatedAt = 310L,
            ),
        )

        val stored = database.memoryItemDao().getById("memory-3")
        val space = database.spaceDao().getById("space-1")

        assertThat(stored?.spaceId).isEqualTo("space-1")
        assertThat(space?.name).isEqualTo("Networking")
    }
}
