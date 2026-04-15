package com.example.zenithapp20.ui.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.utils.MensajesNotificacion
import java.util.Calendar

class NotificacionAguaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val META_ML = 2000
    }

    override suspend fun doWork(): Result {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // Solo actúa entre las 8 AM y las 9 PM
        if (hora < 8 || hora > 21) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val totalMlHoy = db.aguaDao().getTotalMlHoy(hoy) ?: 0

        // Si ya alcanzó la meta, no molestes
        if (totalMlHoy >= META_ML) return Result.success()

        val mlRestantes = META_ML - totalMlHoy
        val vasosRestantes = mlRestantes / 250

        // Personaliza el mensaje según qué tan cerca está de la meta
        val (titulo, mensaje) = when {
            totalMlHoy == 0 -> Pair(
                "💧 Aún no has tomado agua hoy",
                "Empieza ahora. Un vaso es todo lo que se necesita para arrancar. Ábrelo en Zenith."
            )
            vasosRestantes == 1 -> Pair(
                "💧 Te falta 1 vaso para la meta",
                "Solo uno más. ${totalMlHoy}ml de ${META_ML}ml. Termínalo y cierra el día hidratado."
            )
            else -> MensajesNotificacion.obtenerMensaje(
                context = applicationContext,
                pool = MensajesNotificacion.mensajesAgua,
                poolKey = "agua"
            )
        }

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 2003,
            canal = NotificationHelper.CHANNEL_MAÑANA,
            titulo = titulo,
            mensaje = mensaje
        )

        return Result.success()
    }
}