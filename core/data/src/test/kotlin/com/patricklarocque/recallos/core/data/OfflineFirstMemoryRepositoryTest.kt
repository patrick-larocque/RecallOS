package com.patricklarocque.recallos.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.patricklarocque.recallos.core.database.RecallOsDatabase
import com.patricklarocque.recallos.core.database.dao.MemoryItemDao
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity
import com.patricklarocque.recallos.core.files.MemoryFileStore
import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.files.StoredRawContent
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.core.model.ProcessingStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineFirstMemoryRepositoryTest {
    private lateinit var database: RecallOsDatabase
    private lateinit var fileStore: RecordingMemoryFileStore
    private lateinit var repository: OfflineFirstMemoryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RecallOsDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        fileStore = RecordingMemoryFileStore()
        repository = OfflineFirstMemoryRepository(
            database = database,
            memoryItemDao = database.memoryItemDao(),
            fileStore = fileStore,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveRawMemoryPersistsRawContentBeforeMetadata() = runBlocking {
        val saved = repository.saveRawMemory(
            NewMemoryInput(
                type = MemoryType.TEXT_SNIPPET,
                title = "Copied command",
                rawContent = RawMemoryContent(
                    bytes = "adb devices".encodeToByteArray(),
                    originalFileName = "clipboard.txt",
                    mimeType = "text/plain",
                ),
            ),
        )

        val storedEntity = database.memoryItemDao().getById(saved.id)
        assertThat(fileStore.savedMemoryItemIds).containsExactly(saved.id)
        assertThat(storedEntity?.rawContentPath).isEqualTo("/raw/${saved.id}")
        assertThat(storedEntity?.processingStatus).isEqualTo(ProcessingStatus.PENDING.name)
        assertThat(saved.extractedText).isNull()
    }

    @Test
    fun saveRawMemoryDeletesRawContentWhenMetadataWriteFails() {
        runBlocking {
            val failingRepository = OfflineFirstMemoryRepository(
                database = database,
                memoryItemDao = FailingMemoryItemDao(),
                fileStore = fileStore,
            )

            val result = runCatching {
                failingRepository.saveRawMemory(
                    NewMemoryInput(
                        type = MemoryType.NOTE,
                        title = "Draft note",
                        rawContent = RawMemoryContent(
                            bytes = "raw note".encodeToByteArray(),
                            originalFileName = "note.txt",
                        ),
                    ),
                )
            }

            assertThat(result.isFailure).isTrue()
            assertThat(fileStore.savedMemoryItemIds).hasSize(1)
            assertThat(fileStore.deletedPaths).containsExactly("/raw/${fileStore.savedMemoryItemIds.single()}")
        }
    }

    private class RecordingMemoryFileStore : MemoryFileStore {
        val savedMemoryItemIds = mutableListOf<String>()
        val deletedPaths = mutableListOf<String>()

        override suspend fun saveRawContent(
            memoryItemId: String,
            content: RawMemoryContent,
        ): StoredRawContent {
            savedMemoryItemIds += memoryItemId
            return StoredRawContent(
                path = "/raw/$memoryItemId",
                sizeBytes = content.bytes.size.toLong(),
                sha256 = "test-sha",
            )
        }

        override suspend fun deleteRawContent(path: String) {
            deletedPaths += path
        }
    }

    private class FailingMemoryItemDao : MemoryItemDao {
        override suspend fun upsert(item: MemoryItemEntity) {
            error("metadata write failed")
        }

        override suspend fun getById(id: String): MemoryItemEntity? = null

        override suspend fun getAll(): List<MemoryItemEntity> = emptyList()
    }
}
