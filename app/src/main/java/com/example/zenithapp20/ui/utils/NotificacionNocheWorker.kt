package com.example.zenithapp20.ui.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
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
            // ¡Todo completado! Refuerzo positivo
            NotificationHelper.mostrarNotificacion(
                context = applicationContext,
                id = 1005,
                canal = NotificationHelper.CHANNEL_MAÑANA,
                titulo = "✅ Día perfecto",
                mensaje = "Completaste todos tus hábitos hoy. Eso es disciplina real. Vuelve mañana y sigue construyendo."
            )
            return Result.success()
        }

        val enRiesgo = pendientes.filter { it.rachaDias > 0 }
        val maxRacha = enRiesgo.maxOfOrNull { it.rachaDias } ?: 0

        val (titulo, mensaje) = when {
            maxRacha >= 14 -> Pair(
                "🚨 $maxRacha días de racha a punto de romperse",
                "Llevas $maxRacha días seguidos y esta noche podrías perderlo todo. Abre Zenith ahora. Te toma menos de un minuto marcar tus hábitos."
            )
            enRiesgo.isNotEmpty() -> Pair(
                "🔥 Última oportunidad — rachas en riesgo",
                "${enRiesgo.size} racha${if (enRiesgo.size != 1) "s" else ""} a punto de romperse: ${enRiesgo.take(2).joinToString(", ") { "${it.nombre} (${it.rachaDias}d)" }}. Son las 8 PM. No lo dejes para las 11."
            )
            else -> Pair(
                "🌙 Quedan pocas horas",
                "${pendientes.size} hábitos sin completar hoy. Mañana no cuenta. Abre la app y cierra el día como corresponde."
            )
        }

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