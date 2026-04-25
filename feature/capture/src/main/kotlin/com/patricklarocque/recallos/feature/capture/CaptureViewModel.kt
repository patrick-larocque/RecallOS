package com.patricklarocque.recallos.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patricklarocque.recallos.core.data.MemoryRepository
import com.patricklarocque.recallos.core.data.NewMemoryInput
import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.model.MemoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
        _uiState.update {
            it.copy(
                title = title.orEmpty(),
                body = body.orEmpty(),
            )
        }
    }

    fun save(type: MemoryType = MemoryType.NOTE) {
        val state = _uiState.value
        if (state.body.isBlank()) return
        if (state.saveState is CaptureState.Saving) return

        _uiState.update { it.copy(saveState = CaptureState.Saving) }

        viewModelScope.launch {
            val result = runCatching {
                memoryRepository.saveRawMemory(
                    NewMemoryInput(
                        type = type,
                        title = state.title.ifBlank { null },
                        rawContent = RawMemoryContent(
                            bytes = state.body.encodeToByteArray(),
                            originalFileName = null,
                            mimeType = "text/plain",
                        ),
                    ),
                )
            }
            _uiState.update { current ->
                current.copy(
                    saveState = result.fold(
                        onSuccess = { item -> CaptureState.Success(item.id) },
                        onFailure = { t -> CaptureState.Error(t.message ?: "Save failed") },
                    ),
                )
            }
        }
    }

    fun acknowledgeSuccess() {
        _uiState.update { it.copy(saveState = CaptureState.Idle) }
    }
}
