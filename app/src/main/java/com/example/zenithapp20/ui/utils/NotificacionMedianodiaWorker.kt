package com.example.zenithapp20.ui.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.utils.MensajesNotificacion
import java.util.Calendar

class NotificacionMedianodiaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hora < 12 || hora > 13) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val habitos = db.habitosDao().getAllHabitosSync()
        if (habitos.isEmpty()) return Result.success()

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val completados = habitos.count { h ->
            h.checks.any { it >= hoy && it < hoy + 86400000 }
        }
        val pendientes = habitos.size - completados

        // If everything is done, don't disturb the user
        if (pendientes == 0) return Result.success()

        val (titulo, mensaje) = MensajesNotificacion.obtenerMensaje(
            context = applicationContext,
            pool = MensajesNotificacion.mensajesMediadia,
            poolKey = "mediodia"
        )

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 1003,
            canal = NotificationHelper.CHANNEL_HABITOS,
            titulo = titulo,
            mensaje = mensaje
        )

        return Result.success()
    }
}