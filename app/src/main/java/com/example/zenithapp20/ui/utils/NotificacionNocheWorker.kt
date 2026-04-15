package com.example.zenithapp20.ui.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.utils.MensajesNotificacion
import java.util.Calendar

class NotificacionNocheWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hora < 20 || hora > 21) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val habitos = db.habitosDao().getAllHabitosSync()
        if (habitos.isEmpty()) return Result.success()

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val pendientes = habitos.filter { h ->
            h.checks.none { it >= hoy && it < hoy + 86400000 }
        }

        if (pendientes.isEmpty()) {
            val (titulo, mensaje) = MensajesNotificacion.obtenerMensaje(
                context = applicationContext,
                pool = MensajesNotificacion.mensajesExito,
                poolKey = "exito"
            )
            NotificationHelper.mostrarNotificacion(
                context = applicationContext,
                id = 1005,
                canal = NotificationHelper.CHANNEL_MAÑANA,
                titulo = titulo,
                mensaje = mensaje
            )
            return Result.success()
        }

        val enRiesgo = pendientes.filter { it.rachaDias > 0 }

        val (titulo, mensaje) = MensajesNotificacion.obtenerMensaje(
            context = applicationContext,
            pool = MensajesNotificacion.mensajesNoche,
            poolKey = "noche"
        )

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 1005,
            canal = NotificationHelper.CHANNEL_URGENTE,
            titulo = titulo,
            mensaje = mensaje,
            esUrgente = enRiesgo.isNotEmpty()
        )

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 1005,
            canal = NotificationHelper.CHANNEL_URGENTE,
            titulo = titulo,
            mensaje = mensaje,
            esUrgente = enRiesgo.isNotEmpty()
        )

        return Result.success()
    }
}