package com.patricklarocque.recallos.feature.capture

import com.google.common.truth.Truth.assertThat
import com.patricklarocque.recallos.core.data.MemoryRepository
import com.patricklarocque.recallos.core.data.NewMemoryInput
import com.patricklarocque.recallos.core.model.MemoryItem
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.core.model.ProcessingStatus
import com.patricklarocque.recallos.core.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeMemoryRepository
    private lateinit var viewModel: CaptureViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMemoryRepository()
        viewModel = CaptureViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveNoteMemory_callsRepositoryAndEmitsSuccess() = runTest {
        viewModel.setBody("My first note")
        viewModel.save(MemoryType.NOTE)

        val state = viewModel.uiState.value
        assertThat(state.saveState).isInstanceOf(CaptureState.Success::class.java)
        assertThat(fakeRepository.savedInputs).hasSize(1)
        assertThat(fakeRepository.savedInputs.single().type).isEqualTo(MemoryType.NOTE)
    }

    @Test
    fun saveTextSnippet_setsCorrectMemoryType() = runTest {
        viewModel.setBody("copied snippet")
        viewModel.save(MemoryType.TEXT_SNIPPET)

        assertThat(fakeRepository.savedInputs.single().type).isEqualTo(MemoryType.TEXT_SNIPPET)
    }

    @Test
    fun saveEmptyBody_doesNotCallRepository() = runTest {
        viewModel.setBody("")
        viewModel.save(MemoryType.NOTE)

        assertThat(fakeRepository.savedInputs).isEmpty()
        assertThat(viewModel.uiState.value.saveState).isEqualTo(CaptureState.Idle)
    }

    @Test
    fun repositoryFailure_emitsError() = runTest {
        fakeRepository.shouldThrow = true
        viewModel.setBody("some text")
        viewModel.save(MemoryType.NOTE)

        val state = viewModel.uiState.value.saveState
        assertThat(state).isInstanceOf(CaptureState.Error::class.java)
    }

    @Test
    fun prefill_setsInitialBodyAndTitle() {
        viewModel.prefill(title = "Shared Title", body = "Shared body text")

        assertThat(viewModel.uiState.value.title).isEqualTo("Shared Title")
        assertThat(viewModel.uiState.value.body).isEqualTo("Shared body text")
    }

    @Test
    fun acknowledgeSuccess_resetsStateToIdle() = runTest {
        viewModel.setBody("note")
        viewModel.save(MemoryType.NOTE)
        assertThat(viewModel.uiState.value.saveState).isInstanceOf(CaptureState.Success::class.java)

        viewModel.acknowledgeSuccess()
        assertThat(viewModel.uiState.value.saveState).isEqualTo(CaptureState.Idle)
    }

    @Test
    fun titleIsNullWhenBlank() = runTest {
        viewModel.setTitle("")
        viewModel.setBody("body text")
        viewModel.save(MemoryType.NOTE)

        assertThat(fakeRepository.savedInputs.single().title).isNull()
    }

    @Test
    fun titleIsPreservedWhenProvided() = runTest {
        viewModel.setTitle("My Title")
        viewModel.setBody("body text")
        viewModel.save(MemoryType.NOTE)

        assertThat(fakeRepository.savedInputs.single().title).isEqualTo("My Title")
    }

    private class FakeMemoryRepository : MemoryRepository {
        val savedInputs = mutableListOf<NewMemoryInput>()
        var shouldThrow = false
        private var idCounter = 0

        override suspend fun saveRawMemory(input: NewMemoryInput): MemoryItem {
            if (shouldThrow) error("save failed")
            savedInputs += input
            return fakeMemoryItem(id = "id-${idCounter++}", type = input.type)
        }

        override suspend fun getMemory(id: String): MemoryItem? = null
        override suspend fun getRecentMemories(limit: Int): List<MemoryItem> = emptyList()
        override suspend fun updateProcessingStatus(
            id: String,
            status: ProcessingStatus,
            failureReason: String?,
        ): MemoryItem? = null
        override suspend fun updateExtractedText(id: String, extractedText: String?): MemoryItem? = null
        override suspend fun deleteMemory(id: String) = Unit

        private fun fakeMemoryItem(id: String, type: MemoryType) = MemoryItem(
            id = id,
            type = type,
            title = null,
            rawContentPath = "/raw/$id",
            originalFileName = null,
            mimeType = "text/plain",
            sizeBytes = 0L,
            sha256 = null,
            extractedText = null,
            processingStatus = ProcessingStatus.PENDING,
            syncStatus = SyncStatus.LOCAL_ONLY,
            failureReason = null,
            spaceId = null,
            sourceUri = null,
            capturedAt = 0L,
            createdAt = 0L,
            updatedAt = 0L,
        )
    }
}
