package com.patricklarocque.recallos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.patricklarocque.recallos.core.data.MemoryRepository
import com.patricklarocque.recallos.core.data.NewMemoryInput
import com.patricklarocque.recallos.core.files.MemoryFileStore
import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.feature.capture.CaptureScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    @Inject
    lateinit var memoryRepository: MemoryRepository

    @Inject
    lateinit var fileStore: MemoryFileStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action != Intent.ACTION_SEND) {
            finish()
            return
        }

        val mimeType = intent.type ?: ""

        when {
            mimeType == "text/plain" -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                val sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                setContent {
                    CaptureScreen(
                        onSaved = { finish() },
                        prefillTitle = sharedTitle,
                        prefillBody = sharedText,
                    )
                }
            }
            mimeType.startsWith("image/") || mimeType.startsWith("application/") -> {
                @Suppress("DEPRECATION")
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri == null) {
                    finish()
                    return
                }
                val type = if (mimeType.startsWith("image/")) MemoryType.IMAGE else MemoryType.FILE
                MainScope().launch {
                    try {
                        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: run { finish(); return@launch }
                        memoryRepository.saveRawMemory(
                            NewMemoryInput(
                                type = type,
                                title = null,
                                rawContent = RawMemoryContent(
                                    bytes = bytes,
                                    mimeType = mimeType,
                                    originalFileName = uri.lastPathSegment,
                                ),
                            ),
                        )
                    } finally {
                        finish()
                    }
                }
            }
            else -> finish()
        }
    }
}
