package com.example.zenithapp20

import android.app.Application
import androidx.work.*
import com.example.zenithapp20.utils.NotificationHelper
import com.example.zenithapp20.utils.RachaEnRiesgoWorker
import com.example.zenithapp20.utils.RecordatorioMañanaWorker
import java.util.concurrent.TimeUnit
import java.util.Calendar

class ZenithApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanales(this)
        programarNotificaciones()
    }

    private fun programarNotificaciones() {
        val workManager = WorkManager.getInstance(this)

        // RECORDATORIO MATUTINO — cada 24 horas con delay inicial hasta las 8:00 AM
        val ahora = Calendar.getInstance()
        val objetivo8am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (ahora.after(objetivo8am)) objetivo8am.add(Calendar.DAY_OF_YEAR, 1)
        val delayMañana = objetivo8am.timeInMillis - ahora.timeInMillis

        val recordatorioRequest = PeriodicWorkRequestBuilder<RecordatorioMañanaWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delayMañana, TimeUnit.MILLISECONDS)
            .addTag("recordatorio_mañana")
            .build()

        // ALERTA DE RACHA — cada 24 horas con delay inicial hasta las 9:00 PM
        val objetivo9pm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (ahora.after(objetivo9pm)) objetivo9pm.add(Calendar.DAY_OF_YEAR, 1)
        val delayNoche = objetivo9pm.timeInMillis - ahora.timeInMillis

        val rachaRequest = PeriodicWorkRequestBuilder<RachaEnRiesgoWorker>(
            15, TimeUnit.MINUTES
        )
            .addTag("racha_en_riesgo")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "recordatorio_mañana",
            ExistingPeriodicWorkPolicy.KEEP,
            recordatorioRequest  // ← el worker que faltaba
        )
        workManager.enqueueUniquePeriodicWork(
            "racha_en_riesgo",
            ExistingPeriodicWorkPolicy.KEEP,
            rachaRequest
        )
    }
}