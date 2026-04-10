package com.example.zenithapp20.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.zenithapp20.R

object NotificationHelper {

    const val CHANNEL_HABITOS = "zenith_habitos"
    const val CHANNEL_RECORDATORIO = "zenith_recordatorio"

    fun crearCanales(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val canalHabitos = NotificationChannel(
            CHANNEL_HABITOS,
            "Hábitos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas de rachas en riesgo"
        }

        val canalRecordatorio = NotificationChannel(
            CHANNEL_RECORDATORIO,
            "Recordatorio diario",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Resumen matutino del día"
        }

        manager.createNotificationChannel(canalHabitos)
        manager.createNotificationChannel(canalRecordatorio)
    }

    fun mostrarNotificacion(
        context: Context,
        id: Int,
        canal: String,
        titulo: String,
        mensaje: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notificacion)
    }
}