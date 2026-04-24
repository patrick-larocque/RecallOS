package com.patricklarocque.recallos.core.data

import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.model.MemoryType

data class NewMemoryInput(
    val type: MemoryType,
    val title: String?,
    val rawContent: RawMemoryContent,
    val spaceId: String? = null,
)
