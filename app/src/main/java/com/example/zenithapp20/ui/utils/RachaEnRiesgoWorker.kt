package com.example.zenithapp20.ui.utils

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
        // Solo actúa entre las 22:30 y las 23:59 — el aviso más agresivo del día
        val cal = Calendar.getInstance()
        val hora = cal.get(Calendar.HOUR_OF_DAY)
        val minuto = cal.get(Calendar.MINUTE)
        if (hora < 22 || (hora == 22 && minuto < 30)) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val habitos = db.habitosDao().getAllHabitosSync()
        if (habitos.isEmpty()) return Result.success()

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val enRiesgo = habitos.filter { h ->
            val tieneRacha = h.rachaDias > 0
            val noCompletado = h.checks.none { it >= hoy && it < hoy + 86400000 }
            tieneRacha && noCompletado
        }

        if (enRiesgo.isEmpty()) return Result.success()

        val maxRacha = enRiesgo.maxOf { it.rachaDias }
        val nombres = enRiesgo.take(2).joinToString(" y ") { "${it.nombre} (${it.rachaDias}d)" }

        val titulo = when {
            maxRacha >= 30 -> "💀 $maxRacha días se van a perder esta noche"
            maxRacha >= 14 -> "🚨 Son las 10:30 PM — $maxRacha días en juego"
            else -> "⏰ Último aviso del día"
        }

        val mensaje = "Quedan menos de 90 minutos. ${enRiesgo.size} racha${if (enRiesgo.size != 1) "s" else ""} se romperá${if (enRiesgo.size != 1) "n" else ""} a medianoche: $nombres. No dejes que el sueño te haga perder lo que construiste."

        NotificationHelper.mostrarNotificacion(
            context = applicationContext,
            id = 1002,
            canal = NotificationHelper.CHANNEL_URGENTE,
            titulo = titulo,
            mensaje = mensaje,
            esUrgente = true
        )

        return Result.success()
    }
}