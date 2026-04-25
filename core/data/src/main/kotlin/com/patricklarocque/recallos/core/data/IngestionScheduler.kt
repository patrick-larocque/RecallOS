package com.patricklarocque.recallos.core.data

interface IngestionScheduler {
    fun schedule(memoryItemId: String)
}
