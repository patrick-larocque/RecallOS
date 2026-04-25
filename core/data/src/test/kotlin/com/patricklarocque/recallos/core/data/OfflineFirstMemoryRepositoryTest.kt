package com.patricklarocque.recallos.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.patricklarocque.recallos.core.database.RecallOsDatabase
import com.patricklarocque.recallos.core.database.dao.MemoryItemDao
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity
import com.patricklarocque.recallos.core.database.entity.SpaceEntity
import com.patricklarocque.recallos.core.files.MemoryFileStore
import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.files.StoredRawContent
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.core.model.ProcessingStatus
import com.patricklarocque.recallos.core.model.SyncStatus
import java.io.InputStream
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
        database.spaceDao().upsert(
            SpaceEntity(
                id = "space-1",
                name = "Clipboard",
                createdAt = 100L,
            ),
        )

        val saved = repository.saveRawMemory(
            NewMemoryInput(
                type = MemoryType.TEXT_SNIPPET,
                title = "Copied command",
                rawContent = RawMemoryContent(
                    bytes = "adb devices".encodeToByteArray(),
                    originalFileName = "clipboard.txt",
                    mimeType = "text/plain",
                ),
                spaceId = "space-1",
                sourceUri = "content://clipboard/item",
                capturedAt = 123L,
            ),
        )

        val storedEntity = database.memoryItemDao().getById(saved.id)
        assertThat(fileStore.savedMemoryItemIds).containsExactly(saved.id)
        assertThat(storedEntity?.rawContentPath).isEqualTo("/raw/${saved.id}")
        assertThat(storedEntity?.originalFileName).isEqualTo("clipboard.txt")
        assertThat(storedEntity?.mimeType).isEqualTo("text/plain")
        assertThat(storedEntity?.sizeBytes).isEqualTo("adb devices".encodeToByteArray().size.toLong())
        assertThat(storedEntity?.sha256).isEqualTo("test-sha")
        assertThat(storedEntity?.processingStatus).isEqualTo(ProcessingStatus.PENDING.name)
        assertThat(storedEntity?.syncStatus).isEqualTo(SyncStatus.LOCAL_ONLY.name)
        assertThat(storedEntity?.spaceId).isEqualTo("space-1")
        assertThat(storedEntity?.sourceUri).isEqualTo("content://clipboard/item")
        assertThat(storedEntity?.capturedAt).isEqualTo(123L)
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

    @Test
    fun saveRawMemoryDoesNotWriteMetadataWhenRawContentSaveFails() {
        runBlocking {
            val failingRepository = OfflineFirstMemoryRepository(
                database = database,
                memoryItemDao = database.memoryItemDao(),
                fileStore = ThrowingMemoryFileStore(),
            )

            val result = runCatching {
                failingRepository.saveRawMemory(
                    NewMemoryInput(
                        type = MemoryType.FILE,
                        title = "Broken file",
                        rawContent = RawMemoryContent(
                            bytes = "raw".encodeToByteArray(),
                            originalFileName = "broken.txt",
                        ),
                    ),
                )
            }

            assertThat(result.isFailure).isTrue()
            assertThat(database.memoryItemDao().getAll()).isEmpty()
        }
    }

    @Test
    fun repositoryReadsUpdatesAndDeletesMemory() {
        runBlocking {
            val saved = repository.saveRawMemory(
                NewMemoryInput(
                    type = MemoryType.NOTE,
                    title = "Status note",
                    rawContent = RawMemoryContent(
                        bytes = "status".encodeToByteArray(),
                        originalFileName = "status.txt",
                    ),
                ),
            )

            val updatedStatus = repository.updateProcessingStatus(
                id = saved.id,
                status = ProcessingStatus.FAILED,
                failureReason = "OCR failed",
            )
            val updatedText = repository.updateExtractedText(
                id = saved.id,
                extractedText = "extracted text",
            )
            val recent = repository.getRecentMemories(limit = 10)

            assertThat(repository.getMemory(saved.id)?.id).isEqualTo(saved.id)
            assertThat(updatedStatus?.processingStatus).isEqualTo(ProcessingStatus.FAILED)
            assertThat(updatedStatus?.failureReason).isEqualTo("OCR failed")
            assertThat(updatedText?.extractedText).isEqualTo("extracted text")
            assertThat(recent.map { item -> item.id }).contains(saved.id)

            repository.deleteMemory(saved.id)

            assertThat(repository.getMemory(saved.id)).isNull()
            assertThat(fileStore.deletedPaths).containsExactly("/raw/${saved.id}")
        }
    }

    private open class RecordingMemoryFileStore : MemoryFileStore {
        val savedMemoryItemIds = mutableListOf<String>()
        val deletedPaths = mutableListOf<String>()

        override suspend fun saveRawContent(
            memoryItemId: String,
            content: RawMemoryContent,
        ): StoredRawContent {
            return saveRawContent(
                memoryItemId = memoryItemId,
                originalFileName = content.originalFileName,
                mimeType = content.mimeType,
                inputStream = content.bytes.inputStream(),
            )
        }

        override suspend fun saveRawContent(
            memoryItemId: String,
            originalFileName: String?,
            mimeType: String?,
            inputStream: InputStream,
        ): StoredRawContent {
            savedMemoryItemIds += memoryItemId
            val bytes = inputStream.use { stream -> stream.readBytes() }
            return StoredRawContent(
                path = "/raw/$memoryItemId",
                sizeBytes = bytes.size.toLong(),
                sha256 = "test-sha",
            )
        }

        override suspend fun openRawContent(path: String): InputStream {
            return ByteArray(0).inputStream()
        }

        override suspend fun deleteRawContent(path: String) {
            deletedPaths += path
        }
    }

    private class ThrowingMemoryFileStore : RecordingMemoryFileStore() {
        override suspend fun saveRawContent(
            memoryItemId: String,
            originalFileName: String?,
            mimeType: String?,
            inputStream: InputStream,
        ): StoredRawContent {
            error("raw content write failed")
        }
    }

    private class FailingMemoryItemDao : MemoryItemDao {
        override suspend fun upsert(item: MemoryItemEntity) {
            error("metadata write failed")
        }

        override suspend fun getById(id: String): MemoryItemEntity? = null

        override suspend fun getAll(): List<MemoryItemEntity> = emptyList()

        override suspend fun getRecent(limit: Int): List<MemoryItemEntity> = emptyList()

        override suspend fun updateProcessingStatus(
            id: String,
            status: String,
            failureReason: String?,
            updatedAt: Long,
        ) = Unit

        override suspend fun updateExtractedText(
            id: String,
            extractedText: String?,
            updatedAt: Long,
        ) = Unit

        override suspend fun deleteById(id: String) = Unit
    }
}
