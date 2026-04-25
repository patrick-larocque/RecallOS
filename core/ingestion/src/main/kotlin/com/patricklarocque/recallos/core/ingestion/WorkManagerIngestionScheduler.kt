package com.patricklarocque.recallos.core.ingestion

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.patricklarocque.recallos.core.data.IngestionScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkManagerIngestionScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : IngestionScheduler {

    override fun schedule(memoryItemId: String) {
        val request = OneTimeWorkRequestBuilder<IngestionWorker>()
            .setInputData(workDataOf(IngestionWorker.KEY_MEMORY_ITEM_ID to memoryItemId))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
