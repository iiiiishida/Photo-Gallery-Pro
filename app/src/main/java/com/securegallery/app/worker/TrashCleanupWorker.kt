package com.securegallery.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.securegallery.app.SecureGalleryApp
import com.securegallery.app.data.AppSettings

class TrashCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SecureGalleryApp ?: return Result.failure()
        val settingsDao = app.database.appSettingsDao()
        val trashDao = app.database.trashDao()
        val days = settingsDao.get("trash_days")?.toIntOrNull() ?: 30
        val before = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000
        trashDao.deleteOlderThan(before)
        return Result.success()
    }
}
