package com.patricklarocque.recallos.core.ingestion

import com.patricklarocque.recallos.core.data.IngestionScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class IngestionModule {

    @Binds
    abstract fun bindIngestionScheduler(
        impl: WorkManagerIngestionScheduler,
    ): IngestionScheduler

    @Binds
    abstract fun bindOcrProcessor(
        impl: MlKitOcrProcessor,
    ): OcrProcessor

    @Binds
    abstract fun bindChunkingService(
        impl: TextChunkingService,
    ): ChunkingService

    @Binds
    abstract fun bindEmbeddingService(
        impl: NoOpEmbeddingService,
    ): EmbeddingService
}
