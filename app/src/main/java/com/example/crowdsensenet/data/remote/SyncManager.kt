package com.example.crowdsensenet.data.remote

import android.content.Context
import androidx.constraintlayout.widget.Constraints

object SyncManager {

    fun startSync(context: Context) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest =
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}