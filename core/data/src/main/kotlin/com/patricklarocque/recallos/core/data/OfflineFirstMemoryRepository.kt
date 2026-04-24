package com.patricklarocque.recallos.core.data

import androidx.room.withTransaction
import com.patricklarocque.recallos.core.database.RecallOsDatabase
import com.patricklarocque.recallos.core.database.dao.MemoryItemDao
import com.patricklarocque.recallos.core.database.entity.MemoryItemEntity
import com.patricklarocque.recallos.core.files.MemoryFileStore
import com.patricklarocque.recallos.core.model.MemoryItem
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.core.model.ProcessingStatus
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
            extractedText = null,
            processingStatus = ProcessingStatus.PENDING.name,
            spaceId = input.spaceId,
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
}

private fun MemoryItemEntity.toModel(): MemoryItem {
    return MemoryItem(
        id = id,
        type = MemoryType.valueOf(type),
        title = title,
        rawContentPath = rawContentPath,
        extractedText = extractedText,
        processingStatus = ProcessingStatus.valueOf(processingStatus),
        spaceId = spaceId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
