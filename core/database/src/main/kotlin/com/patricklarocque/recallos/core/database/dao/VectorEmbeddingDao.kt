package com.patricklarocque.recallos.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.patricklarocque.recallos.core.database.entity.VectorEmbeddingEntity

@Dao
interface VectorEmbeddingDao {
    @Upsert
    suspend fun upsert(embedding: VectorEmbeddingEntity)

    @Query("SELECT * FROM vector_embeddings WHERE chunk_id = :chunkId")
    suspend fun getForChunk(chunkId: String): VectorEmbeddingEntity?

    @Query("SELECT * FROM vector_embeddings WHERE chunk_id IN (:chunkIds)")
    suspend fun getForChunks(chunkIds: List<String>): List<VectorEmbeddingEntity>

    @Query("DELETE FROM vector_embeddings WHERE chunk_id = :chunkId")
    suspend fun deleteForChunk(chunkId: String)
}
