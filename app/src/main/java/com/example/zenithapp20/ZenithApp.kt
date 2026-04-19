package com.example.zenithapp20

import android.app.Application
import com.example.zenithapp20.ui.utils.NotificationHelper
import com.example.zenithapp20.utils.BootReceiver

class ZenithApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanales(this)
        // Delegates scheduling to BootReceiver so logic lives in one place
        BootReceiver.reprogramarNotificaciones(this)
    }
}