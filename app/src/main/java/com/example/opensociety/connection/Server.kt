package com.example.opensociety.connection

import android.content.Context
import android.util.Log
import com.example.opensociety.connection.stun.StunClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.lang.Exception
import java.net.*
import java.util.*

class Server(context: Context) {
    private val TAG = "Server"

    private val SERVER_PORT = 9876

    private val context = context
    var serverSoket: ServerSocket? = null
    var port = SERVER_PORT
    var serverIP: InetAddress? = null

    sealed class StatusMsg {
        object IncCounter : StatusMsg()
        object DisCounter : StatusMsg()
        object Stopped : StatusMsg()
        object Working : StatusMsg()
        object GoToStop : StatusMsg()
        object GoToWork : StatusMsg()
        class GetCounter(val response: SendChannel<Int>) : StatusMsg()
        class GetWorkStatus(val response: Channel<Status>) : StatusMsg()
    }

    enum class Status {
        WORKING,
        STOPPED,
        GOTOSTOP,
        GOTOWORK;
        companion object {
            fun intToStatus(value: Int) = Status.values()[value]
            fun names() = Status.values().map { it.name }
        }
    }

    companion object {
        fun getIPAddress(isIP4: Boolean = true): InetAddress? {
            try {
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    val addrs: List<InetAddress> = Collections.list(intf.getInetAddresses())
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress() ) {
                            //val sAddr: String = addr.getHostAddress()
                            when(isIP4) {
                                true -> if (addr is Inet4Address) {
                                    return addr
                                }
                                false -> if (addr is Inet6Address) {
                                    return addr
                                    /**/
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
        fun addresInString():String {
            val addr = getIPAddress()
            if (addr is Inet6Address) {
                val sa = addr.getHostAddress()
                val delim = sa.indexOf('%') // drop ip6 port suffix
                return if (delim < 0) sa else sa.substring(0, delim)
            }
            return addr.toString()
        }
    }

    val actor = GlobalScope.actor<StatusMsg> {
        var counter = 0
        var status = Status.STOPPED
        for (msg in channel) {
            when (msg) {
                is StatusMsg.IncCounter -> counter++
                is StatusMsg.DisCounter -> counter--
                is StatusMsg.GetCounter -> msg.response.send(counter)

                is StatusMsg.GoToStop -> {
                    Log.d(TAG, "actor gets GoToStop")
                    if(status == Status.WORKING) {
                        status = Status.GOTOSTOP
                        //Log.d(TAG, "joining to main thread")
                        //mainThread!!.join()
                        Log.d(TAG, "mainThread ste to null")
                        mainThread = null
                        if (serverSoket != null) {
                            Log.d(TAG, "server socket is bound")
                            serverSoket!!.close()
                            serverSoket = null
                        }
                        Log.d(TAG, "stop: success stop main thread")
                        status = Status.STOPPED
                    }
                }
                is StatusMsg.GoToWork -> {
                    Log.d(TAG, "actor gets GoToWork: current status $status")
                    if(status == Status.STOPPED) {
                        status = Status.GOTOWORK
                        val prepare = GlobalScope.async {
                            Log.d(TAG, "start stun to find external IP and Port")
                            val stun = StunClient(getIPAddress()!!, port)
                            stun.getAddress()!!.also {serverIP = it.first; port = it.second }
                            status = Status.WORKING
                        }
                        mainThread = GlobalScope.launch {
                            Log.d (TAG, "wait stun")
                            prepare.await()
                            Log.d(TAG, "main thread: started")
                            server()
                            Log.d(TAG, "main thread: going to stop waiting jobs")
                            waitJobsEnd()
                            Log.d(TAG, "main thread: stop")
                        }
                        Log.d(TAG, "start: main thread is started Status: $status")
                    }
                }
                is StatusMsg.GetWorkStatus -> msg.response.send(status)
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

    suspend fun getStatus(counter: SendChannel<StatusMsg>): Status {
        val response = Channel<Status>()
        counter.send(StatusMsg.GetWorkStatus(response))
        val receive = response.receive()
        Log.d(TAG, "getStatus: $receive")
        return receive
    }

    protected suspend fun finalize() {
        Log.d(TAG, "finalize")
        stop()
    }

    fun stop(){
        GlobalScope.launch {
            Log.d(TAG, "stop: status is ${getStatus(actor)}")
            /*if(getStatus(actor) == Status.GOTOWORK) {
                Log.d(TAG, "stop: status is GOTOWOR so wait")
                waitStatus(Status.WORKING)
            }
            if(getStatus(actor) == Status.WORKING) {*/
                Log.d(TAG, "stop: going to stop")
                actor.send(StatusMsg.GoToStop)
            //}
        }
    }

    private var mainThread: Job? = null

    private suspend fun waitStatus(status: Status) {
        Log.d(TAG, "current status: ${getStatus(actor)} waiting $status")
            while (getStatus(actor) != status){
                delay(100)}
    }

    private suspend fun waitJobsEnd() {
            while (getCurrentCount(actor) != 0){
                delay(100)}
    }

    fun start() {
        GlobalScope.launch {
            /*if (getStatus(actor) == Status.GOTOSTOP) {
                Log.d(TAG, "start: status is GOTOSTOP so wait")
                waitStatus(Status.STOPPED)
            }
            if (getStatus(actor) == Status.STOPPED) {*/
                Log.d(TAG, "start: going to work")
                actor.send(StatusMsg.GoToWork)
            //}
        }
    }

     /*init {
         start()
     }*/

    suspend fun status() = getStatus(actor)

    suspend fun startWorker(worker: ConnectionWorker) {
         GlobalScope.launch {
            actor.send(StatusMsg.IncCounter)
            var workID = getCurrentCount(actor)
            Log.d(TAG, "start work $workID")
            worker.run()
            Log.d(TAG, "stop work $workID")
            actor.send(StatusMsg.DisCounter)
        }
    }

    suspend fun server(): String {
        waitStatus(Status.WORKING)
        Log.d(TAG, "Server start on port : $port")
        serverSoket = ServerSocket(port,50, serverIP)

        Log.d(TAG, "Server status:${getStatus(actor)}")
        while (getStatus(actor) == Status.WORKING) {
            try {
                Log.d(TAG, "Waiting client")
                serverSoket?.let {
                    startWorker(ConnectionWorker(it.accept(), context))} ?: null
                Log.d(TAG, "task started go to next")

            } catch (e: Exception) {
                Log.d(TAG, "Connection error : ${e.message}")
                serverSoket?.isClosed
                break
            }
        }
        Log.d(TAG, "Thread '${Thread.currentThread().name}' stoped")
        serverSoket?.close()
        return "" + Thread.currentThread().name
    }

}