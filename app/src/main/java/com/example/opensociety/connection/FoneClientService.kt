package com.example.opensociety.connection

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlin.concurrent.thread
import android.app.NotificationManager

import android.R
import android.app.NotificationChannel
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import android.net.Network

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkCapabilities

import android.net.NetworkRequest
import android.provider.Settings
import com.example.opensociety.db.Contacts
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception


class FoneClientService : Service() {
    val TAG = "FoneClientService"
    private var server:Server = Server(this)
    val CHANNEL_ID = "OpenSocietyServerService"

    override fun onCreate() {
        super.onCreate()
        registerNetworkCallback()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sentNotification(message: String) {
        var notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var channel = NotificationChannel(CHANNEL_ID, "OpenSociety server chanel",
            NotificationManager.IMPORTANCE_HIGH)
        channel.setDescription("OpenSociety server channel notify about the current server status");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
        val builder: NotificationCompat.Builder? = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle(TAG)
            .setContentText(message)
        val notification: Notification = builder!!.build()
        startForeground(101, notification);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun registerNetworkCallback() {
        try {
            val connectivityManager =
                this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()
            connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
                var contacts = Contacts(this@FoneClientService)
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "onAvailable: starting server")
                    sentNotification("online")
                        server.start()

                    contacts.updateIP()
                }
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onLost(network: Network) {
                    Log.d(TAG, "onLost: stopping server")
                    sentNotification("offline")
                        server.stop()
                }
            }
            )
        } catch (e: Exception) {
            sentNotification("Server not started. reason: $e")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}