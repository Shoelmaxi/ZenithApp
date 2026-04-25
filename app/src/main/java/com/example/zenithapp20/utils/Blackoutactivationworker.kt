package com.example.zenithapp20.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker disparado por WorkManager cuando llega la hora de dormir programada.
 * Lanza el BlackoutOverlayService.
 */
class BlackoutActivationWorker(
    context: Context,
    params:  WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        BlackoutOverlayService.iniciar(applicationContext)
        return Result.success()
    }
}