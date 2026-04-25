package com.patricklarocque.recallos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.patricklarocque.recallos.core.data.MemoryRepository
import com.patricklarocque.recallos.core.data.NewMemoryInput
import com.patricklarocque.recallos.core.designsystem.theme.RecallOsTheme
import com.patricklarocque.recallos.core.files.RawMemoryContent
import com.patricklarocque.recallos.core.model.MemoryType
import com.patricklarocque.recallos.feature.capture.CaptureScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    @Inject
    lateinit var memoryRepository: MemoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action != Intent.ACTION_SEND) {
            finish()
            return
        }

        val mimeType = intent.type.orEmpty()

        when {
            mimeType == "text/plain" -> handleTextShare(intent)
            mimeType.startsWith("image/") || mimeType.startsWith("application/") -> handleBinaryShare(intent, mimeType)
            else -> finish()
        }
    }

    private fun handleTextShare(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            RecallOsTheme {
                CaptureScreen(
                    onSaved = { finish() },
                    prefillBody = sharedText,
                )
            }
        }
    }

    private fun handleBinaryShare(intent: Intent, mimeType: String) {
        @Suppress("DEPRECATION")
        val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        if (uri == null) {
            finish()
            return
        }

        val type = if (mimeType.startsWith("image/")) MemoryType.IMAGE else MemoryType.FILE
        val fileName = uri.lastPathSegment

        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Cannot read shared content")
                    memoryRepository.saveRawMemory(
                        NewMemoryInput(
                            type = type,
                            title = fileName,
                            rawContent = RawMemoryContent(
                                bytes = bytes,
                                originalFileName = fileName,
                                mimeType = mimeType,
                            ),
                            sourceUri = uri.toString(),
                        ),
                    )
                }
            }
            if (result.isSuccess || result.isFailure) {
                finish()
            }
        }
    }
}
