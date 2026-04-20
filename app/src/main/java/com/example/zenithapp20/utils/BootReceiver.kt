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

        // En reboot siempre recalculamos delays → REPLACE
        reprogramarNotificaciones(context)
    }

    companion object {

        /**
         * Llamado desde BootReceiver (reinicio del dispositivo).
         * Usa REPLACE para recalcular los delays desde la hora actual.
         */
        fun reprogramarNotificaciones(context: Context) {
            programar(context, ExistingPeriodicWorkPolicy.REPLACE)
        }

        /**
         * Llamado desde ZenithApp.onCreate() (apertura normal de la app).
         * Usa KEEP para no cancelar workers ya activos y no saltar notificaciones
         * del día en curso si el usuario abre la app después de la hora programada.
         */
        fun registrarNotificacionesSiNoExisten(context: Context) {
            programar(context, ExistingPeriodicWorkPolicy.KEEP)
        }

        private fun programar(context: Context, policy: ExistingPeriodicWorkPolicy) {
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

            wm.enqueueUniquePeriodicWork(
                "notif_mañana",
                policy,
                PeriodicWorkRequestBuilder<RecordatorioMañanaWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(8), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_mediodia",
                policy,
                PeriodicWorkRequestBuilder<NotificacionMedianodiaWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(12, 30), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_tarde",
                policy,
                PeriodicWorkRequestBuilder<NotificacionTardeWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(17), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_noche",
                policy,
                PeriodicWorkRequestBuilder<NotificacionNocheWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(20), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_racha",
                policy,
                PeriodicWorkRequestBuilder<RachaEnRiesgoWorker>(15, TimeUnit.MINUTES)
                    .setInitialDelay(delayHasta(22, 30), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_finanzas",
                policy,
                PeriodicWorkRequestBuilder<NotificacionFinanzasWorker>(12, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(10), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_gym",
                policy,
                PeriodicWorkRequestBuilder<NotificacionGymWorker>(8, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(9), TimeUnit.MILLISECONDS)
                    .build()
            )
            wm.enqueueUniquePeriodicWork(
                "notif_agua",
                policy,
                PeriodicWorkRequestBuilder<NotificacionAguaWorker>(2, TimeUnit.HOURS)
                    .setInitialDelay(delayHasta(8), TimeUnit.MILLISECONDS)
                    .build()
            )
        }
    }
}