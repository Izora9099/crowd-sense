package com.example.crowdsensenet.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.crowdsensenet.data.local.AppDatabase
import com.example.crowdsensenet.data.remote.FirebaseRepository

class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.measurementDao()
        val repo = FirebaseRepository()

        val pending = dao.getPendingMeasurements()

        for (m in pending) {
            try {
                repo.uploadMeasurement(m)
                dao.markAsUploaded(m.id)
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        return Result.success()
    }
}