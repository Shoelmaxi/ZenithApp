package com.example.zenithapp20.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.zenithapp20.MainActivity
import com.example.zenithapp20.R

object NotificationHelper {

    const val CHANNEL_HABITOS   = "zenith_habitos"
    const val CHANNEL_URGENTE   = "zenith_urgente"
    const val CHANNEL_MAÑANA    = "zenith_mañana"

    fun crearCanales(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal normal — recordatorio matutino y mediodía
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_MAÑANA, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Resumen y recordatorios del día"
            }
        )

        // Canal importante — alerta de racha
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_HABITOS, "Hábitos", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alertas de hábitos pendientes"
            }
        )

        // Canal máxima prioridad — última llamada / racha a punto de romperse
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_URGENTE, "Urgente", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Última oportunidad para no romper tu racha"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300, 200, 500)
                enableLights(true)
            }
        )
    }

    fun mostrarNotificacion(
        context: Context,
        id: Int,
        canal: String,
        titulo: String,
        mensaje: String,
        esUrgente: Boolean = false
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Al tocar la notificación abre la app directamente
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(
                if (esUrgente) NotificationCompat.PRIORITY_MAX
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .apply {
                if (esUrgente) {
                    setVibrate(longArrayOf(0, 300, 200, 300, 200, 500))
                }
            }
            .build()

        manager.notify(id, notificacion)
    }
}