package com.example.opensociety.connection

import java.net.Socket

import android.util.Log
import java.io.IOException


class Connection(host: String, port: Int) {
    private final val LOG_TAG = "Connection"
    private var socket: Socket? = null
    private var host = host
    private var port = port

    public fun openConnection() {
        closeConnection();
        try {
            socket = Socket(host, port)
        } catch (e: IOException) {
            throw Exception("couldn't create socket: " +
                    e.message)
        }
    }

    public fun closeConnection() {
        if (socket != null && !socket!!.isClosed) {
            try {
                socket!!.close()
            } catch (e: IOException) {
                Log.e(
                    LOG_TAG, "There is an error in closing:"
                            + e.message
                )
            } finally {
                socket = null
            }
        }
        socket = null
    }

    public fun sendData(data: ByteArray) {
        if (socket == null || socket!!.isClosed) {
            throw Exception(
                "Error of sending data. " +
                        "the socket is not available"
            )
        }
        try {
            socket!!.getOutputStream().write(data)
            socket!!.getOutputStream().flush()
        } catch (e: IOException) {
            throw Exception(
                "Ошибка отправки данных : "
                        + e.message
            )
        }
    }
    protected fun finalize() {
        closeConnection();
    }
}