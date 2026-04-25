package com.patricklarocque.recallos.core.ingestion

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.patricklarocque.recallos.core.data.MemoryRepository
import com.patricklarocque.recallos.core.database.VectorBlobConverter
import com.patricklarocque.recallos.core.database.dao.MemoryChunkDao
import com.patricklarocque.recallos.core.database.dao.VectorEmbeddingDao
import com.patricklarocque.recallos.core.database.entity.MemoryChunkEntity
import com.patricklarocque.recallos.core.database.entity.VectorEmbeddingEntity
import com.patricklarocque.recallos.core.files.MemoryFileStore
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.core.model.ProcessingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

@HiltWorker
class IngestionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val memoryRepository: MemoryRepository,
    private val fileStore: MemoryFileStore,
    private val ocrProcessor: OcrProcessor,
    private val chunkingService: ChunkingService,
    private val embeddingService: EmbeddingService,
    private val memoryChunkDao: MemoryChunkDao,
    private val vectorEmbeddingDao: VectorEmbeddingDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val memoryItemId = inputData.getString(KEY_MEMORY_ITEM_ID)
            ?: return Result.failure()
        val memoryItem = memoryRepository.getMemory(memoryItemId)
            ?: return Result.failure()

        return try {
            val text: String? = when (memoryItem.type) {
                MemoryType.IMAGE, MemoryType.PHOTO, MemoryType.SCREENSHOT -> {
                    val path = memoryItem.rawContentPath ?: return Result.failure()
                    val bytes = fileStore.openRawContent(path).use { it.readBytes() }
                    ocrProcessor.extractText(bytes)
                }
                MemoryType.NOTE, MemoryType.TEXT_SNIPPET, MemoryType.LINK -> {
                    memoryItem.extractedText
                        ?: memoryItem.rawContentPath?.let { path ->
                            fileStore.openRawContent(path).use { it.readBytes() }
                                .toString(Charsets.UTF_8)
                        }
                }
                else -> null
            }

            if (!text.isNullOrBlank()) {
                memoryRepository.updateExtractedText(memoryItemId, text)
                val now = System.currentTimeMillis()
                val chunks = chunkingService.chunk(text)
                val chunkEntities = chunks.map { chunk ->
                    MemoryChunkEntity(
                        id = UUID.randomUUID().toString(),
                        memoryItemId = memoryItemId,
                        text = chunk.text,
                        chunkIndex = chunk.chunkIndex,
                        embeddingRef = null,
                        createdAt = now,
                    )
                }
                memoryChunkDao.upsert(chunkEntities)
                chunkEntities.forEach { chunkEntity ->
                    val vector = embeddingService.embed(chunkEntity.text)
                    vectorEmbeddingDao.upsert(
                        VectorEmbeddingEntity(
                            chunkId = chunkEntity.id,
                            modelId = embeddingService.modelId,
                            dimensions = embeddingService.dimensions,
                            vectorBlob = VectorBlobConverter.fromFloatArray(vector),
                            createdAt = now,
                        ),
                    )
                }
            }

            memoryRepository.updateProcessingStatus(memoryItemId, ProcessingStatus.READY)
            Result.success()
        } catch (e: Exception) {
            memoryRepository.updateProcessingStatus(
                id = memoryItemId,
                status = ProcessingStatus.FAILED,
                failureReason = e.message,
            )
            Result.failure()
        }
    }

    companion object {
        const val KEY_MEMORY_ITEM_ID = "memory_item_id"
    }
}
