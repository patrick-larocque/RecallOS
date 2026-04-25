package com.patricklarocque.recallos.core.files

data class StoredRawContent(
    val path: String,
    val sizeBytes: Long,
    val sha256: String,
)
