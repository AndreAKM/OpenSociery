package com.example.opensociety.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.opensociety.db.Contacts

class FoneServiceReceiver : BroadcastReceiver() {
    val TAG = "FoneServiceReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "catch ${intent.action.toString()}")
        val intentService = ServiceCommandBuilder(context)
            .setCommand(ServiceCommandBuilder.Command.START).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
        Contacts(context).updateIP()
    }
}