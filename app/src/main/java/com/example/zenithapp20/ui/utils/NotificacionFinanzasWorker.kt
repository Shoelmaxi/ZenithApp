package com.example.zenithapp20.ui.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.utils.MensajesNotificacion
import java.util.Calendar

class NotificacionFinanzasWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // Solo actúa a las 10 AM o a las 7 PM
        val esHoraValida = hora in 10..11 || hora in 19..20
        if (!esHoraValida) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val transacciones = db.finanzasDao().getAllTransaccionesSync()

        // Comprueba si ya hay alguna transacción registrada hoy
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tieneRegistroHoy = transacciones.any { it.fechaMillis >= hoy }
        if (tieneRegistroHoy) return Result.success()

        val (titulo, mensaje) = MensajesNotificacion.obtenerMensaje(
            context = applicationContext,
            pool = MensajesNotificacion.mensajesFinanzas,
            poolKey = "finanzas"
        )

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 2001,
            canal = NotificationHelper.CHANNEL_HABITOS,
            titulo = titulo,
            mensaje = mensaje
        )

        return Result.success()
    }
}