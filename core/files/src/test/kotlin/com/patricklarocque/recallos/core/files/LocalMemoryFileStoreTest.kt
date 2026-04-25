package com.patricklarocque.recallos.core.files

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocalMemoryFileStoreTest {
    @Test
    fun saveRawContentPersistsFileUnderAppStorage() = runBlocking {
        val fileStore = LocalMemoryFileStore(
            context = ApplicationProvider.getApplicationContext(),
        )

        val stored = fileStore.saveRawContent(
            memoryItemId = "memory-1",
            content = RawMemoryContent(
                bytes = "saved before ingestion".encodeToByteArray(),
                originalFileName = "capture.txt",
                mimeType = "text/plain",
            ),
        )

        val storedFile = File(stored.path)
        assertThat(storedFile.exists()).isTrue()
        assertThat(storedFile.readText()).isEqualTo("saved before ingestion")
        assertThat(stored.sizeBytes).isEqualTo(storedFile.length())
        assertThat(stored.path).contains("recallos/raw/memory-1")
    }

    @Test
    fun saveRawContentSanitizesUnsafeFileNamesAndComputesSha256() = runBlocking {
        val fileStore = LocalMemoryFileStore(
            context = ApplicationProvider.getApplicationContext(),
        )
        val bytes = "sanitize me".encodeToByteArray()

        val stored = fileStore.saveRawContent(
            memoryItemId = "memory-unsafe",
            content = RawMemoryContent(
                bytes = bytes,
                originalFileName = "../bad name?.txt",
                mimeType = "text/plain",
            ),
        )

        val storedFile = File(stored.path)
        assertThat(storedFile.parentFile?.path).contains("recallos/raw/memory-unsafe")
        assertThat(storedFile.name).isEqualTo("bad_name_.txt")
        assertThat(storedFile.readText()).isEqualTo("sanitize me")
        assertThat(stored.sha256).isEqualTo(bytes.sha256())
    }

    @Test
    fun openRawContentReturnsPersistedFileStream() = runBlocking {
        val fileStore = LocalMemoryFileStore(
            context = ApplicationProvider.getApplicationContext(),
        )
        val stored = fileStore.saveRawContent(
            memoryItemId = "memory-open",
            originalFileName = "open.txt",
            mimeType = "text/plain",
            inputStream = "streamed content".byteInputStream(),
        )

        val openedText = fileStore.openRawContent(stored.path).use { input ->
            input.readBytes().decodeToString()
        }

        assertThat(openedText).isEqualTo("streamed content")
    }

    @Test
    fun deleteRawContentRemovesPersistedFile() = runBlocking {
        val fileStore = LocalMemoryFileStore(
            context = ApplicationProvider.getApplicationContext(),
        )
        val stored = fileStore.saveRawContent(
            memoryItemId = "memory-delete",
            content = RawMemoryContent(
                bytes = "delete me".encodeToByteArray(),
                originalFileName = "delete.txt",
            ),
        )

        fileStore.deleteRawContent(stored.path)

        assertThat(File(stored.path).exists()).isFalse()
    }

    private fun ByteArray.sha256(): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(this)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
