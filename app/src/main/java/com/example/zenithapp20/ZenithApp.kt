package com.example.zenithapp20

import android.app.Application
import androidx.work.*
import com.example.zenithapp20.ui.utils.*
import com.example.zenithapp20.ui.utils.`RecordatorioMañanaWorker`
import com.example.zenithapp20.utils.BootReceiver
import java.util.concurrent.TimeUnit
import java.util.Calendar

class ZenithApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanales(this)
        // Delegamos la programación al mismo método que usa el BootReceiver
        // así no hay duplicación de lógica
        BootReceiver.reprogramarNotificaciones(this)
    }

    private fun programarNotificaciones() {
        val wm = WorkManager.getInstance(this)

        fun delayHasta(horaObj: Int, minutoObj: Int = 0): Long {
            val ahora = Calendar.getInstance()
            val objetivo = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, horaObj)
                set(Calendar.MINUTE, minutoObj)
                set(Calendar.SECOND, 0)
            }
            if (ahora.after(objetivo)) objetivo.add(Calendar.DAY_OF_YEAR, 1)
            return objetivo.timeInMillis - ahora.timeInMillis
        }

        // 8:00 AM — Arranque motivacional
        wm.enqueueUniquePeriodicWork(
            "notif_mañana",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<`RecordatorioMañanaWorker`>(24, TimeUnit.HOURS)
                .setInitialDelay(delayHasta(8), TimeUnit.MILLISECONDS)
                .build()
        )

        // 12:30 PM — Control de mediodía
        wm.enqueueUniquePeriodicWork(
            "notif_mediodia",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificacionMedianodiaWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayHasta(12, 30), TimeUnit.MILLISECONDS)
                .build()
        )

        // 5:00 PM — Empujón de tarde
        wm.enqueueUniquePeriodicWork(
            "notif_tarde",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificacionTardeWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayHasta(17), TimeUnit.MILLISECONDS)
                .build()
        )

        // 8:00 PM — Última llamada
        wm.enqueueUniquePeriodicWork(
            "notif_noche",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificacionNocheWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayHasta(20), TimeUnit.MILLISECONDS)
                .build()
        )

        // 10:30 PM — Aviso agresivo de racha (cada 15 min, filtra por hora internamente)
        wm.enqueueUniquePeriodicWork(
            "notif_racha",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RachaEnRiesgoWorker>(15, TimeUnit.MINUTES)
                .setInitialDelay(delayHasta(22, 30), TimeUnit.MILLISECONDS)
                .build()
        )
    }
}