package com.patricklarocque.recallos.core.database

import java.nio.ByteBuffer
import java.nio.ByteOrder

object VectorBlobConverter {
    fun fromFloatArray(vector: FloatArray): ByteArray {
        val buffer = ByteBuffer
            .allocate(vector.size * Float.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
        vector.forEach(buffer::putFloat)
        return buffer.array()
    }

    fun toFloatArray(blob: ByteArray): FloatArray {
        require(blob.size % Float.SIZE_BYTES == 0) {
            "Vector blob size must be divisible by ${Float.SIZE_BYTES}"
        }
        val buffer = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(blob.size / Float.SIZE_BYTES) {
            buffer.float
        }
    }
}
