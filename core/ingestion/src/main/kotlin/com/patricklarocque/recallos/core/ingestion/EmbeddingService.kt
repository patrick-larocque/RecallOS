package com.patricklarocque.recallos.core.ingestion

interface EmbeddingService {
    val modelId: String
    val dimensions: Int
    suspend fun embed(text: String): FloatArray
}
