package com.patricklarocque.recallos.core.ingestion

import javax.inject.Inject

class NoOpEmbeddingService @Inject constructor() : EmbeddingService {
    override val modelId = "noop-v0"
    override val dimensions = 384
    override suspend fun embed(text: String): FloatArray = FloatArray(dimensions)
}
