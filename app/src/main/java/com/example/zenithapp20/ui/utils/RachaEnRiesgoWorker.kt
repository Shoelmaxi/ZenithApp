package com.example.zenithapp20.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import java.util.Calendar

class RachaEnRiesgoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // solo actúa entre las 21:00 y las 23:59
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (horaActual < 21) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val habitos = db.habitosDao().getAllHabitosSync()

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val enRiesgo = habitos.filter { habito ->
            val tieneRacha = habito.rachaDias > 0
            val noCompletadoHoy = habito.checks.none { it >= hoy && it < hoy + 86400000 }
            tieneRacha && noCompletadoHoy
        }

        if (enRiesgo.isNotEmpty()) {
            val nombres = enRiesgo.take(2).joinToString(", ") { it.nombre }
            val mensaje = if (enRiesgo.size == 1)
                "¡Tu racha de '$nombres' está en riesgo!"
            else
                "¡Rachas en riesgo: $nombres${if (enRiesgo.size > 2) " y más..." else ""}"

            NotificationHelper.mostrarNotificacion(
                context = applicationContext,
                id = 1002,
                canal = NotificationHelper.CHANNEL_HABITOS,
                titulo = "🔥 Racha en riesgo",
                mensaje = mensaje
            )
        }

        return Result.success()
    }
}