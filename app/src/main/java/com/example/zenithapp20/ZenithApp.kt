package com.example.zenithapp20

import android.app.Application
import com.example.zenithapp20.ui.utils.NotificationHelper
import com.example.zenithapp20.utils.BootReceiver
import com.example.zenithapp20.utils.DeepWorkForegroundService
import com.example.zenithapp20.utils.WorkoutForegroundService

class ZenithApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanales(this)
        WorkoutForegroundService.crearCanal(this)
        DeepWorkForegroundService.crearCanal(this)
        BootReceiver.registrarNotificacionesSiNoExisten(this)
    }
}