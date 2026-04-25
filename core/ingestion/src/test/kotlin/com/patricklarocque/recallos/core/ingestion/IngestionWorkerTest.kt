package com.patricklarocque.recallos.core.ingestion

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.patricklarocque.recallos.core.data.MemoryRepository
import com.patricklarocque.recallos.core.data.NewMemoryInput
import com.patricklarocque.recallos.core.database.dao.MemoryChunkDao
import com.patricklarocque.recallos.core.database.dao.VectorEmbeddingDao
import com.patricklarocque.recallos.core.database.entity.MemoryChunkEntity
import com.patricklarocque.recallos.core.database.entity.VectorEmbeddingEntity
import com.patricklarocque.recallos.core.files.MemoryFileStore
import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.files.StoredRawContent
import com.patricklarocque.recallos.core.model.MemoryItem
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.core.model.ProcessingStatus
import com.patricklarocque.recallos.core.model.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class IngestionWorkerTest {

    private lateinit var context: Context
    private lateinit var fakeRepository: FakeMemoryRepository
    private lateinit var fakeFileStore: FakeMemoryFileStore
    private lateinit var fakeChunkDao: FakeMemoryChunkDao
    private lateinit var fakeEmbeddingDao: FakeVectorEmbeddingDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fakeRepository = FakeMemoryRepository()
        fakeFileStore = FakeMemoryFileStore()
        fakeChunkDao = FakeMemoryChunkDao()
        fakeEmbeddingDao = FakeVectorEmbeddingDao()
    }

    private fun buildWorker(memoryItemId: String): IngestionWorker {
        return TestListenableWorkerBuilder<IngestionWorker>(context)
            .setInputData(
                androidx.work.workDataOf(IngestionWorker.KEY_MEMORY_ITEM_ID to memoryItemId),
            )
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters,
                ): ListenableWorker = IngestionWorker(
                    context = appContext,
                    params = workerParameters,
                    memoryRepository = fakeRepository,
                    fileStore = fakeFileStore,
                    ocrProcessor = FakeOcrProcessor(),
                    chunkingService = TextChunkingService(),
                    embeddingService = NoOpEmbeddingService(),
                    memoryChunkDao = fakeChunkDao,
                    vectorEmbeddingDao = fakeEmbeddingDao,
                )
            })
            .build() as IngestionWorker
    }

    @Test
    fun missingMemoryItemId_returnsFailure() = runBlocking {
        val worker = TestListenableWorkerBuilder<IngestionWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters,
                ): ListenableWorker = IngestionWorker(
                    context = appContext,
                    params = workerParameters,
                    memoryRepository = fakeRepository,
                    fileStore = fakeFileStore,
                    ocrProcessor = FakeOcrProcessor(),
                    chunkingService = TextChunkingService(),
                    embeddingService = NoOpEmbeddingService(),
                    memoryChunkDao = fakeChunkDao,
                    vectorEmbeddingDao = fakeEmbeddingDao,
                )
            })
            .build()

        val result = worker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun noteMemory_chunksAndStoresEmbeddings() = runBlocking {
        val memoryItem = fakeMemoryItem(id = "m1", type = MemoryType.NOTE)
        fakeRepository.items["m1"] = memoryItem
        fakeFileStore.contents["raw/m1"] = "This is a test note with some content".toByteArray()

        val worker = buildWorker("m1")
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(fakeChunkDao.upsertedChunks).isNotEmpty()
        assertThat(fakeEmbeddingDao.upsertedEmbeddings).hasSize(fakeChunkDao.upsertedChunks.size)
        assertThat(fakeRepository.statusUpdates["m1"]).isEqualTo(ProcessingStatus.READY)
    }

    @Test
    fun unknownMemoryItem_returnsFailure() = runBlocking {
        val worker = buildWorker("nonexistent")
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun repositoryThrows_updatesStatusToFailed() = runBlocking {
        val memoryItem = fakeMemoryItem(id = "m2", type = MemoryType.NOTE)
        fakeRepository.items["m2"] = memoryItem
        fakeFileStore.shouldThrow = true

        val worker = buildWorker("m2")
        worker.doWork()

        assertThat(fakeRepository.statusUpdates["m2"]).isEqualTo(ProcessingStatus.FAILED)
    }

    @Test
    fun imageMemory_doesNotChunkWithoutOcrResult() = runBlocking {
        val memoryItem = fakeMemoryItem(id = "m3", type = MemoryType.IMAGE)
        fakeRepository.items["m3"] = memoryItem
        fakeFileStore.contents["raw/m3"] = ByteArray(10)

        val worker = buildWorker("m3")
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(fakeChunkDao.upsertedChunks).isEmpty()
    }

    private fun fakeMemoryItem(id: String, type: MemoryType) = MemoryItem(
        id = id,
        type = type,
        title = null,
        rawContentPath = "raw/$id",
        originalFileName = null,
        mimeType = "text/plain",
        sizeBytes = 0L,
        sha256 = null,
        extractedText = null,
        processingStatus = ProcessingStatus.PENDING,
        syncStatus = SyncStatus.LOCAL_ONLY,
        failureReason = null,
        spaceId = null,
        sourceUri = null,
        capturedAt = 0L,
        createdAt = 0L,
        updatedAt = 0L,
    )

    private class FakeOcrProcessor : OcrProcessor {
        override suspend fun extractText(imageBytes: ByteArray): String? = null
    }

    private class FakeMemoryRepository : MemoryRepository {
        val items = mutableMapOf<String, MemoryItem>()
        val statusUpdates = mutableMapOf<String, ProcessingStatus>()

        override suspend fun saveRawMemory(input: NewMemoryInput): MemoryItem =
            error("not used in worker tests")

        override suspend fun getMemory(id: String): MemoryItem? = items[id]
        override suspend fun getRecentMemories(limit: Int): List<MemoryItem> = emptyList()

        override suspend fun updateProcessingStatus(
            id: String,
            status: ProcessingStatus,
            failureReason: String?,
        ): MemoryItem? {
            statusUpdates[id] = status
            return items[id]
        }

        override suspend fun updateExtractedText(id: String, extractedText: String?): MemoryItem? =
            items[id]

        override suspend fun deleteMemory(id: String) = Unit
    }

    private class FakeMemoryFileStore : MemoryFileStore {
        val contents = mutableMapOf<String, ByteArray>()
        var shouldThrow = false

        override suspend fun saveRawContent(
            memoryItemId: String,
            content: RawMemoryContent,
        ): StoredRawContent = error("not used")

        override suspend fun saveRawContent(
            memoryItemId: String,
            originalFileName: String?,
            mimeType: String?,
            inputStream: InputStream,
        ): StoredRawContent = error("not used")

        override suspend fun openRawContent(path: String): InputStream {
            if (shouldThrow) error("read failed")
            val bytes = contents[path] ?: error("no content for $path")
            return bytes.inputStream()
        }

        override suspend fun deleteRawContent(path: String) = Unit
    }

    private class FakeMemoryChunkDao : MemoryChunkDao {
        val upsertedChunks = mutableListOf<MemoryChunkEntity>()

        override suspend fun upsert(chunks: List<MemoryChunkEntity>) {
            upsertedChunks += chunks
        }

        override suspend fun getForMemoryItem(memoryItemId: String): List<MemoryChunkEntity> =
            upsertedChunks.filter { it.memoryItemId == memoryItemId }
    }

    private class FakeVectorEmbeddingDao : VectorEmbeddingDao {
        val upsertedEmbeddings = mutableListOf<VectorEmbeddingEntity>()

        override suspend fun upsert(embedding: VectorEmbeddingEntity) {
            upsertedEmbeddings += embedding
        }

        override suspend fun getForChunk(chunkId: String): VectorEmbeddingEntity? =
            upsertedEmbeddings.find { it.chunkId == chunkId }

        override suspend fun getForChunks(chunkIds: List<String>): List<VectorEmbeddingEntity> =
            upsertedEmbeddings.filter { it.chunkId in chunkIds }

        override suspend fun deleteForChunk(chunkId: String) {
            upsertedEmbeddings.removeAll { it.chunkId == chunkId }
        }
    }
}
