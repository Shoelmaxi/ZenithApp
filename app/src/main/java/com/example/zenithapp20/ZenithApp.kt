package com.example.zenithapp20

import android.app.Application
import com.example.zenithapp20.ui.utils.NotificationHelper
import com.example.zenithapp20.utils.BootReceiver
import com.example.zenithapp20.utils.WorkoutForegroundService

class ZenithApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanales(this)
        // Canal del servicio de entrenamiento
        WorkoutForegroundService.crearCanal(this)
        // KEEP: no reemplaza workers activos cuando el usuario abre la app.
        // El REPLACE sólo ocurre en BootReceiver (reinicio del dispositivo).
        BootReceiver.registrarNotificacionesSiNoExisten(this)
    }
}