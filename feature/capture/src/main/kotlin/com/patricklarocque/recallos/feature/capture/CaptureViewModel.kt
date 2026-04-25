package com.patricklarocque.recallos.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patricklarocque.recallos.core.data.MemoryRepository
import com.patricklarocque.recallos.core.data.NewMemoryInput
import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.model.MemoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CaptureState {
    data object Idle : CaptureState
    data object Saving : CaptureState
    data class Success(val memoryId: String) : CaptureState
    data class Error(val message: String) : CaptureState
}

data class CaptureUiState(
    val title: String = "",
    val body: String = "",
    val saveState: CaptureState = CaptureState.Idle,
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun setBody(body: String) {
        _uiState.update { it.copy(body = body) }
    }

    fun prefill(title: String?, body: String?) {
        _uiState.update { it.copy(title = title ?: "", body = body ?: "") }
    }

    fun save(type: MemoryType = MemoryType.NOTE) {
        val state = _uiState.value
        if (state.body.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(saveState = CaptureState.Saving) }
            try {
                val title = state.title.takeIf { it.isNotBlank() }
                val bytes = state.body.toByteArray(Charsets.UTF_8)
                val input = NewMemoryInput(
                    type = type,
                    title = title,
                    rawContent = RawMemoryContent(
                        bytes = bytes,
                        mimeType = "text/plain",
                        originalFileName = null,
                    ),
                )
                val saved = memoryRepository.saveRawMemory(input)
                _uiState.update { it.copy(saveState = CaptureState.Success(saved.id)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveState = CaptureState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun acknowledgeSuccess() {
        _uiState.update { it.copy(saveState = CaptureState.Idle) }
    }
}
