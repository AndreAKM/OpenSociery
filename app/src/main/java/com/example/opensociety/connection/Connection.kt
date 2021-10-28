package com.example.opensociety.connection

import java.net.Socket

import android.util.Log
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter


class Connection(host: String) {
    private final val TAG = "Connection"
    private var socket: Socket? = null
    private var host = host
    private var port = 9876

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
                    TAG, "There is an error in closing:"
                            + e.message
                )
            } finally {
                socket = null
            }
        }
        socket = null
    }

    public fun sendData(data: String) {
        if (socket == null || socket!!.isClosed) {
            throw Exception(
                "Error of sending data. " +
                        "the socket is not available"
            )
        }
        try {
            val writer = PrintWriter(
                BufferedWriter(OutputStreamWriter(socket!!.getOutputStream())),
                true)
            Log.d(TAG, "writer.write($data)")
            writer.write(data)
            writer.flush()
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