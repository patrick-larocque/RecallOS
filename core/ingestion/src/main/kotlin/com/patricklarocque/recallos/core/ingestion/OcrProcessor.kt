package com.patricklarocque.recallos.core.ingestion

interface OcrProcessor {
    suspend fun extractText(imageBytes: ByteArray): String?
}
