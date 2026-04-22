package com.example.zenithapp20.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.zenithapp20.MainActivity
import com.example.zenithapp20.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WorkoutNotifState(
    val ejercicioNombre: String = "",
    val serieTexto: String = "",
    val timerTexto: String = "",
    val esDescanso: Boolean = false
)

class WorkoutForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "zenith_workout"
        const val NOTIF_ID = 9001
        private const val ACTION_STOP = "ACTION_STOP_WORKOUT"

        private val _estado = MutableStateFlow(WorkoutNotifState())
        val estado = _estado.asStateFlow()

        private val _activo = MutableStateFlow(false)
        val activo = _activo.asStateFlow()

        fun iniciar(context: Context) {
            _activo.value = true
            val intent = Intent(context, WorkoutForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun detener(context: Context) {
            _activo.value = false
            _estado.value = WorkoutNotifState()
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun actualizar(context: Context, nuevo: WorkoutNotifState) {
            _estado.value = nuevo
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotif(context, nuevo))
        }

        fun crearCanal(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Entrenamiento activo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progreso del entrenamiento en curso"
                setShowBadge(false)
                setSound(null, null)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        fun buildNotif(context: Context, state: WorkoutNotifState): Notification {
            val openIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val titulo = if (state.esDescanso)
                "⏱ Descanso  ${state.timerTexto}"
            else
                "💪 ${state.ejercicioNombre}"

            val cuerpo = if (state.esDescanso)
                "A continuación: ${state.ejercicioNombre}"
            else
                state.serieTexto

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setContentIntent(openIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // API 34+ requiere pasar el tipo explícitamente en la llamada a startForeground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIF_ID,
                buildNotif(this, _estado.value),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIF_ID, buildNotif(this, _estado.value))
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        _activo.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}