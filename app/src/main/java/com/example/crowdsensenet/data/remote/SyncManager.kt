package com.example.crowdsensenet.data.remote

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkInfo
import com.example.crowdsensenet.service.UploadWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

object SyncManager {

    private const val SYNC_INTERVAL_HOURS = 6L

    fun startSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraints)
            .addTag("upload_work")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<UploadWorker>(
            SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("periodic_upload")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "periodic_upload",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("upload_work")
        WorkManager.getInstance(context).cancelAllWorkByTag("periodic_upload")
    }

    fun getSyncStatus(context: Context): Flow<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow("upload_work")
    }

    fun isSyncRunning(context: Context): Flow<Boolean> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow("upload_work")
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING }
            }
    }

    fun startImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraints)
            .addTag("immediate_upload")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "immediate_upload",
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun syncOnConnectivityChange(context: Context, isConnected: Boolean) {
        if (isConnected) {
            startSync(context)
        }
    }
}