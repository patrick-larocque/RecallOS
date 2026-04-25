package com.patricklarocque.recallos.core.data

import androidx.room.withTransaction
import com.patricklarocque.recallos.core.database.RecallOsDatabase
import com.patricklarocque.recallos.core.database.dao.MemoryItemDao
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity
import com.patricklarocque.recallos.core.files.MemoryFileStore
import com.patricklarocque.recallos.core.model.MemoryItem
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.core.model.ProcessingStatus
import com.patricklarocque.recallos.core.model.SyncStatus
import java.util.UUID
import javax.inject.Inject

class OfflineFirstMemoryRepository @Inject constructor(
    private val database: RecallOsDatabase,
    private val memoryItemDao: MemoryItemDao,
    private val fileStore: MemoryFileStore,
) : MemoryRepository {
    override suspend fun saveRawMemory(input: NewMemoryInput): MemoryItem {
        val memoryItemId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val storedRawContent = fileStore.saveRawContent(
            memoryItemId = memoryItemId,
            content = input.rawContent,
        )
        val entity = MemoryItemEntity(
            id = memoryItemId,
            type = input.type.name,
            title = input.title,
            rawContentPath = storedRawContent.path,
            originalFileName = input.rawContent.originalFileName,
            mimeType = input.rawContent.mimeType,
            sizeBytes = storedRawContent.sizeBytes,
            sha256 = storedRawContent.sha256,
            extractedText = null,
            processingStatus = ProcessingStatus.PENDING.name,
            syncStatus = SyncStatus.LOCAL_ONLY.name,
            failureReason = null,
            spaceId = input.spaceId,
            sourceUri = input.sourceUri,
            capturedAt = input.capturedAt ?: now,
            createdAt = now,
            updatedAt = now,
        )

        return try {
            database.withTransaction {
                memoryItemDao.upsert(entity)
            }
            entity.toModel()
        } catch (throwable: Throwable) {
            fileStore.deleteRawContent(storedRawContent.path)
            throw throwable
        }
    }

    override suspend fun getMemory(id: String): MemoryItem? {
        return memoryItemDao.getById(id)?.toModel()
    }

    override suspend fun getRecentMemories(limit: Int): List<MemoryItem> {
        return memoryItemDao.getRecent(limit).map { entity -> entity.toModel() }
    }

    override suspend fun updateProcessingStatus(
        id: String,
        status: ProcessingStatus,
        failureReason: String?,
    ): MemoryItem? {
        memoryItemDao.updateProcessingStatus(
            id = id,
            status = status.name,
            failureReason = failureReason,
            updatedAt = System.currentTimeMillis(),
        )
        return getMemory(id)
    }

    override suspend fun updateExtractedText(
        id: String,
        extractedText: String?,
    ): MemoryItem? {
        memoryItemDao.updateExtractedText(
            id = id,
            extractedText = extractedText,
            updatedAt = System.currentTimeMillis(),
        )
        return getMemory(id)
    }

    override suspend fun deleteMemory(id: String) {
        val memoryItem = memoryItemDao.getById(id) ?: return
        memoryItem.rawContentPath?.let { path ->
            fileStore.deleteRawContent(path)
        }
        database.withTransaction {
            memoryItemDao.deleteById(id)
        }
    }
}

private fun MemoryItemEntity.toModel(): MemoryItem {
    return MemoryItem(
        id = id,
        type = MemoryType.valueOf(type),
        title = title,
        rawContentPath = rawContentPath,
        originalFileName = originalFileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        sha256 = sha256,
        extractedText = extractedText,
        processingStatus = ProcessingStatus.valueOf(processingStatus),
        syncStatus = SyncStatus.valueOf(syncStatus),
        failureReason = failureReason,
        spaceId = spaceId,
        sourceUri = sourceUri,
        capturedAt = capturedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
