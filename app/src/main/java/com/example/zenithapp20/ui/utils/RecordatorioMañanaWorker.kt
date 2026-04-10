package com.example.zenithapp20.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import java.util.Calendar

class RecordatorioMañanaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val habitos = db.habitosDao().getAllHabitosSync()

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val pendientes = habitos.count { habito ->
            habito.checks.none { it >= hoy && it < hoy + 86400000 }
        }

        val mensaje = when {
            pendientes == 0 -> "¡Todo al día! Sigue así 💪"
            pendientes == 1 -> "Tienes 1 hábito pendiente hoy"
            else -> "Tienes $pendientes hábitos pendientes hoy"
        }

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 1001,
            canal = NotificationHelper.CHANNEL_RECORDATORIO,
            titulo = "Buenos días ☀️",
            mensaje = mensaje
        )

        return Result.success()
    }
}