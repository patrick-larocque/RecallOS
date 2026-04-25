package com.patricklarocque.recallos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vector_embeddings",
    foreignKeys = [
        ForeignKey(
            entity = MemoryChunkEntity::class,
            parentColumns = ["id"],
            childColumns = ["chunk_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class VectorEmbeddingEntity(
    @PrimaryKey
    @ColumnInfo(name = "chunk_id")
    val chunkId: String,
    @ColumnInfo(name = "model_id")
    val modelId: String,
    val dimensions: Int,
    @ColumnInfo(name = "vector_blob", typeAffinity = ColumnInfo.BLOB)
    val vectorBlob: ByteArray,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
