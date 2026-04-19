package com.example.zenithapp20.ui.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificacionSuenoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 3001,
            canal = NotificationHelper.CHANNEL_HABITOS,
            titulo = "🌙 Checklist de Desconexión — 60 min",
            mensaje = "Es hora de cerrar el día: apaga pantallas, silencia notificaciones, baja la luz. Tu cerebro lo necesita para consolidar lo aprendido hoy."
        )
        return Result.success()
    }
}