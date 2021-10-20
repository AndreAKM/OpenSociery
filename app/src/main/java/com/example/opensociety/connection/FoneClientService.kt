package com.example.opensociety.connection

import android.app.Service
import android.content.Intent
import android.os.IBinder

class FoneClientService : Service() {
    private val server:Server? = null
    override fun onCreate() {
        super.onCreate()
        server = Server()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}