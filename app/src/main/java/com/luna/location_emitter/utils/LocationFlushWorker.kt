package com.luna.location_emitter.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.luna.location_emitter.data.remote.TrackingApi
import com.luna.location_emitter.data.repository.Repository


class LocationFlushWorker(
    context: Context,
    params: WorkerParameters,
    private val repo: Repository,
    private val api: TrackingApi // Retrofit interface
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val batch = repo.getLocationData()
        if (batch.isEmpty()) return Result.success()

        val payload = batch.map {
            mapOf(
                "type" to it.type,
                "seq" to it.seq,
                "lat" to it.latitude,
                "lng" to it.longitude,
                "timestamp" to it.timestamp
            )
        }

        return try {
            api.sendBatch(payload) // suspend function
            repo.flushDB()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
