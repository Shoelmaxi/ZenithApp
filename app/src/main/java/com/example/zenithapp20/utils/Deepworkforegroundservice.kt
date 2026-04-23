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

data class DeepWorkNotifState(
    val timeLeftStr: String = "00:00",
    val intencion: String = "",
    val distracciones: Int = 0,
    val isPaused: Boolean = false
)

class DeepWorkForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "zenith_deep_work"
        const val NOTIF_ID = 9002
        private const val ACTION_STOP = "ACTION_STOP_DEEP_WORK"

        private val _estado = MutableStateFlow(DeepWorkNotifState())
        val estado = _estado.asStateFlow()

        private val _activo = MutableStateFlow(false)
        val activo = _activo.asStateFlow()

        fun iniciar(context: Context) {
            _activo.value = true
            context.startForegroundService(Intent(context, DeepWorkForegroundService::class.java))
        }

        fun detener(context: Context) {
            _activo.value = false
            _estado.value = DeepWorkNotifState()
            context.startService(
                Intent(context, DeepWorkForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
            )
        }

        fun actualizar(context: Context, nuevo: DeepWorkNotifState) {
            _estado.value = nuevo
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotif(context, nuevo))
        }

        fun crearCanal(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Deep Work activo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Contador de sesión Deep Work en curso"
                setShowBadge(false)
                setSound(null, null)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        fun buildNotif(context: Context, state: DeepWorkNotifState): Notification {
            val openIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val titulo = when {
                state.isPaused -> "⏸ Deep Work pausado — ${state.timeLeftStr}"
                else -> "🧠 Deep Work — ${state.timeLeftStr}"
            }

            val cuerpo = buildString {
                if (state.intencion.isNotBlank()) append(state.intencion)
                if (state.distracciones > 0) {
                    if (isNotEmpty()) append(" · ")
                    append("📱 ${state.distracciones} distracción${if (state.distracciones != 1) "es" else ""}")
                }
            }.ifBlank { "Sesión en curso" }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, buildNotif(this, _estado.value), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
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