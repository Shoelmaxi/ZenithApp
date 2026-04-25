package com.example.zenithapp20.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.ui.utils.NotificationHelper
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Sombra Predictiva — corre una vez al día a las 22:00.
 *
 * Lógica:
 *  1. Lee todos los registros de Análisis AAA.
 *  2. Agrupa por día de la semana.
 *  3. Si el día actual tiene ≥2 registros con completado=false o focusLevel<5
 *     → envía una notificación de advertencia ~30 min antes de que ocurra.
 *  4. Si el patrón es muy fuerte (≥3 fallos) → tono urgente.
 */
class SombraPredictiveWorker(
    context: Context,
    params:  WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db       = AppDatabase.getDatabase(applicationContext)
        val analisis = db.analisisHabitoDao().getAll()

        // Lectura síncrona — collect una sola vez
        var registros = emptyList<com.example.zenithapp20.data.model.AnalisisHabito>()
        val job = kotlinx.coroutines.MainScope().launch {
            analisis.collect { registros = it }
        }
        kotlinx.coroutines.delay(300)
        job.cancel()

        if (registros.isEmpty()) return Result.success()

        val hoy = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        // Filtrar registros del mismo día de la semana
        val registrosDiaActual = registros.filter { registro ->
            val cal = Calendar.getInstance().apply { timeInMillis = registro.fechaMillis }
            cal.get(Calendar.DAY_OF_WEEK) == hoy
        }

        val fallosHistoricos = registrosDiaActual.count { it ->
            !it.completado || it.focusLevel < 5
        }

        if (fallosHistoricos < 2) return Result.success()

        // Calcular estadísticas para el mensaje
        val nombreDia = nombreDiaSemana(hoy)
        val habitoMasFailado = registrosDiaActual
            .filter { !it.completado }
            .groupBy { it.habitoNombre }
            .maxByOrNull { it.value.size }
            ?.key

        val esUrgente = fallosHistoricos >= 3

        val titulo = if (esUrgente)
            "⚠️ Zona de Fallo Detectada — $nombreDia"
        else
            "🔮 Sombra Predictiva — $nombreDia"

        val mensaje = buildString {
            append("Los últimos $fallosHistoricos $nombreDia fallaste con tus hábitos. ")
            habitoMasFailado?.let {
                append("\"$it\" es el más afectado. ")
            }
            append("Prepara el aterrizaje ahora: cierra apps, deja el cargador listo, decide tu próximo movimiento antes de que el cansancio decida por ti.")
        }

        NotificationHelper.mostrarNotificacion(
            context   = applicationContext,
            id        = 5001,
            canal     = if (esUrgente) NotificationHelper.CHANNEL_URGENTE
            else NotificationHelper.CHANNEL_HABITOS,
            titulo    = titulo,
            mensaje   = mensaje,
            esUrgente = esUrgente
        )

        return Result.success()
    }

    private fun nombreDiaSemana(dia: Int): String = when (dia) {
        Calendar.MONDAY    -> "los lunes"
        Calendar.TUESDAY   -> "los martes"
        Calendar.WEDNESDAY -> "los miércoles"
        Calendar.THURSDAY  -> "los jueves"
        Calendar.FRIDAY    -> "los viernes"
        Calendar.SATURDAY  -> "los sábados"
        Calendar.SUNDAY    -> "los domingos"
        else               -> "hoy"
    }
}