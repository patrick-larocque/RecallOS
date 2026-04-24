package com.patricklarocque.recallos.core.files

data class RawMemoryContent(
    val bytes: ByteArray,
    val originalFileName: String? = null,
    val mimeType: String? = null,
)
