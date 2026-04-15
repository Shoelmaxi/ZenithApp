package com.example.zenithapp20.ui.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.utils.MensajesNotificacion
import java.util.Calendar

class NotificacionGymWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val esHoraValida = hora in 9..10 || hora in 17..18
        if (!esHoraValida) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val habitos = db.habitosDao().getAllHabitosSync()

        // Busca el hábito de gym por palabras clave
        val habitoGym = habitos.find { habito ->
            val nombre = habito.nombre.lowercase()
            nombre.contains("entrenar") || nombre.contains("gym") ||
                    nombre.contains("ejercicio") || nombre.contains("entrenamiento")
        } ?: return Result.success() // Si no existe el hábito, no hay nada que recordar

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val completadoHoy = habitoGym.checks.any { it >= hoy && it < hoy + 86400000 }
        if (completadoHoy) return Result.success()

        val (titulo, mensaje) = MensajesNotificacion.obtenerMensaje(
            context = applicationContext,
            pool = MensajesNotificacion.mensajesGym,
            poolKey = "gym"
        )

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 2002,
            canal = NotificationHelper.CHANNEL_HABITOS,
            titulo = titulo,
            mensaje = mensaje
        )

        return Result.success()
    }
}