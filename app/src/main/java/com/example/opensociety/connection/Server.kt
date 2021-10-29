package com.example.opensociety.connection

import android.content.Context
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class Server(context: Context) {
    private val TAG = "Server"

    private val SERVER_PORT = 9876

    private val context = context
    private var serverSoket: ServerSocket? = null

    var started = true
    val port = SERVER_PORT

    sealed class StatusMsg {
        object IncCounter : StatusMsg()
        object DisCounter : StatusMsg()
        object Stopping : StatusMsg()
        class GetCounter(val response: SendChannel<Int>) : StatusMsg()
        class GetWorkStatus(val response: SendChannel<Boolean>) : StatusMsg()
    }

    fun statusActor() = GlobalScope.actor<StatusMsg> {
        var counter = 0
        var isWorking = true
        for (msg in channel) {
            when (msg) {
                is StatusMsg.IncCounter -> counter++
                is StatusMsg.DisCounter -> counter--
                is StatusMsg.GetCounter -> msg.response.send(counter)
                is StatusMsg.Stopping -> isWorking = false
                is StatusMsg.GetWorkStatus -> msg.response.send(isWorking)
            }
        }
    }

    suspend fun getCurrentCount(counter: SendChannel<StatusMsg>): Int {
        val response = Channel<Int>()
        counter.send(StatusMsg.GetCounter(response))
        val receive = response.receive()
        Log.d(TAG, "Counter = $receive")
        return receive
    }

    suspend fun getStatus(counter: SendChannel<StatusMsg>): Boolean {
        val response = Channel<Boolean>()
        counter.send(StatusMsg.GetWorkStatus(response))
        val receive = response.receive()
        Log.d(TAG, "Status = $receive")
        return receive
    }

    protected suspend fun finalize() {
        val status = statusActor()
        status.send(StatusMsg.Stopping)
        mainThread.join()
    }

    private var mainThread = GlobalScope.launch {
        server()
        GlobalScope.async {
            var status = statusActor()
            while (getCurrentCount(status) != 0){
                delay(100)}
        }.await()
    }

    suspend fun startWorker(worker: ConnectionWorker) {
        var wtask = GlobalScope.async {
            var status = statusActor()
            status.send(StatusMsg.IncCounter)
            var workID = getCurrentCount(status)
            Log.d(TAG, "start work $workID")
            worker.run()
            Log.d(TAG, "stop work $workID")
            status.send(StatusMsg.DisCounter)
        }
    }

    suspend fun server(): String {
        serverSoket = ServerSocket(port)
        Log.d(TAG, "Server start on port : $port")
        var status = statusActor()
        while (getStatus(status)) {
            var worker: ConnectionWorker? = null
            try {
                Log.d(TAG, "Ожидание соединения с клиентом")
                serverSoket?.let {
                    startWorker(ConnectionWorker(it.accept(), context))} ?: null

            } catch (e: Exception) {
                Log.d(TAG, "Connection error : ${e.message}")

                if (serverSoket!!.isClosed) break
            }
        }
        Log.d(TAG, "Thread '${Thread.currentThread().name}' stoped")

        return "" + Thread.currentThread().name
    }

}