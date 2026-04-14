package com.example.zenithapp20.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.example.zenithapp20.ui.utils.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        reprogramarNotificaciones(context)
    }

    companion object {
        fun reprogramarNotificaciones(context: Context) {
            val wm = WorkManager.getInstance(context)

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

            // Usamos REPLACE para que al reiniciar se recalculen los delays correctamente
            wm.enqueueUniquePeriodicWork(
                "notif_mañana",
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<RecordatorioMañanaWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(8), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_mediodia",
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<NotificacionMedianodiaWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(12, 30), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_tarde",
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<NotificacionTardeWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(17), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_noche",
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<NotificacionNocheWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(20), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_racha",
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<RachaEnRiesgoWorker>(15, TimeUnit.MINUTES)
                    .setInitialDelay(delayHasta(22, 30), TimeUnit.MILLISECONDS)
                    .build()
            )
        }
    }
}