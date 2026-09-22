package com.example.application

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = TaskApiService.create()

        return try {
            val allTasks = database.taskDao().getAllTasks().first()
            val unsyncedTasks = allTasks.filter { !it.isSynced }

            for (task in unsyncedTasks) {
                val response = apiService.syncTask(task)
                if (response.isSuccessful) {
                    database.taskDao().updateTask(task.copy(isSynced = true))
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}