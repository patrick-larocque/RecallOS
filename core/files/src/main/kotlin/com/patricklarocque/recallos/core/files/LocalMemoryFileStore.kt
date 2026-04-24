package com.patricklarocque.recallos.core.files

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject

class LocalMemoryFileStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : MemoryFileStore {
    override suspend fun saveRawContent(
        memoryItemId: String,
        content: RawMemoryContent,
    ): StoredRawContent {
        val itemDirectory = File(rawContentRoot(), memoryItemId).apply {
            mkdirs()
        }
        val targetFile = File(itemDirectory, buildFileName(content.originalFileName))
        val tempFile = File(itemDirectory, "${targetFile.name}.tmp")

        FileOutputStream(tempFile).use { stream ->
            stream.write(content.bytes)
            stream.channel.force(true)
        }

        if (!tempFile.renameTo(targetFile)) {
            tempFile.delete()
            error("Unable to persist raw content for memory item $memoryItemId")
        }

        return StoredRawContent(
            path = targetFile.absolutePath,
            sizeBytes = targetFile.length(),
            sha256 = sha256(content.bytes),
        )
    }

    override suspend fun deleteRawContent(path: String) {
        File(path).delete()
    }

    private fun rawContentRoot(): File {
        return File(context.filesDir, "recallos/raw").apply {
            mkdirs()
        }
    }

    private fun buildFileName(originalFileName: String?): String {
        val safeName = originalFileName
            ?.substringAfterLast(File.separatorChar)
            ?.replace(Regex("[^A-Za-z0-9._-]"), "_")
            ?.takeIf { it.isNotBlank() }

        return safeName ?: "raw-content.bin"
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
