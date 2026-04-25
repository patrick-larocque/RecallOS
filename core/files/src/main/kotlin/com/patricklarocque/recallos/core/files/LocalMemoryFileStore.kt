package com.patricklarocque.recallos.core.files

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject

class LocalMemoryFileStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : MemoryFileStore {
    override suspend fun saveRawContent(
        memoryItemId: String,
        content: RawMemoryContent,
    ): StoredRawContent {
        return saveRawContent(
            memoryItemId = memoryItemId,
            originalFileName = content.originalFileName,
            mimeType = content.mimeType,
            inputStream = content.bytes.inputStream(),
        )
    }

    override suspend fun saveRawContent(
        memoryItemId: String,
        originalFileName: String?,
        mimeType: String?,
        inputStream: InputStream,
    ): StoredRawContent {
        val itemDirectory = File(rawContentRoot(), memoryItemId).apply {
            mkdirs()
        }
        val targetFile = File(itemDirectory, buildFileName(originalFileName))
        val tempFile = File(itemDirectory, "${targetFile.name}.tmp")
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var sizeBytes = 0L

        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                while (true) {
                    val bytesRead = input.read(buffer)
                    if (bytesRead == -1) break
                    output.write(buffer, 0, bytesRead)
                    digest.update(buffer, 0, bytesRead)
                    sizeBytes += bytesRead
                }
                output.channel.force(true)
            }
        }

        if (!tempFile.renameTo(targetFile)) {
            tempFile.delete()
            error("Unable to persist raw content for memory item $memoryItemId")
        }

        return StoredRawContent(
            path = targetFile.absolutePath,
            sizeBytes = sizeBytes,
            sha256 = digest.digest().toHexString(),
        )
    }

    override suspend fun openRawContent(path: String): InputStream {
        return FileInputStream(File(path))
    }

    override suspend fun deleteRawContent(path: String) {
        val file = File(path)
        if (file.exists() && !file.delete()) {
            error("Unable to delete raw content at $path")
        }
        file.parentFile?.delete()
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

    private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }
}
