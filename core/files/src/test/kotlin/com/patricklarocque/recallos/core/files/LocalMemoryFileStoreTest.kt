package com.patricklarocque.recallos.core.files

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.File
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
}
