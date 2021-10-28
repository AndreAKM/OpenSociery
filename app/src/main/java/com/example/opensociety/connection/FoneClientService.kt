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


class FoneClientService : Service() {
    val TAG = "FoneClientService"
    private var server:Server? = null
    val CHANNEL_ID = "OpenSocietyServerService"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        var notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var channel = NotificationChannel(CHANNEL_ID, "My channel",
                NotificationManager.IMPORTANCE_HIGH)
        channel.setDescription("My channel description");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
        val builder: NotificationCompat.Builder? = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle(TAG)
            .setContentText("starting server")

        val notification: Notification = builder!!.build()
        //notificationManager.notify(101, notification)
       // if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setChannelId(youChannelID);
        startForeground(101, notification);

        Log.d(TAG, "starting server")
        //thread {
            server = Server(this)
            Log.d(TAG, "server started $server")
        //}
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}