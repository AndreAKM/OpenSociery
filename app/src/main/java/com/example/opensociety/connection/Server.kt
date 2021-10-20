package com.example.opensociety.connection

import android.util.Log
import java.lang.Exception
import java.net.ServerSocket
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class Server {
    private val TAG = "Server"

    private val SERVER_PORT = 9876
    private var serverSoket: ServerSocket? = null

    private var callableDelay = CallableDelay()
    private var callableServer = CallableServer(SERVER_PORT)

    private var futureTask = arrayOf(FutureTask<String>(callableDelay), FutureTask<String>(callableServer))

    private var executor = Executors.newFixedThreadPool(2)

    init {
        while (true) {
            if (isTasksDone()) {
                // Завершение работы executor'а
                executor.shutdown();
                System.out.println("\nexecutor shutdown");
                break;
            }
        }
    }

    private fun isTasksDone(): Boolean {
        return futureTask[0].isDone &&
                futureTask[1].isDone
    }
    inner class CallableServer(port: Int) : Callable<String> {
        var started = true
        val port = port

        override public fun call(): String {
            serverSoket = ServerSocket(port)
            Log.d(TAG, "Server start on port : $port")

            while (started) {
                var worker: ConnectionWorker? = null
                try {
                    // Ожидание соединения с клиентом
                    worker = ConnectionWorker(
                        serverSoket.accept()
                    )
                    /*
                 * Обработка соединения выполняется
                 * в отдельном потоке
                 */
                    val t: Thread = Thread(worker)
                    t.start()
                } catch (e: Exception) {
                    System.err.println(
                        "Connection error : "
                                + e.message
                    )
                    // Завершение цикла.
                    if (serverSoket.isClosed()) break
                }
            }
            Log.d(
                TAG,
                "Thread '"
                        + Thread.currentThread().name
                        + "' stoped"
            )

            futureTask.get(1).cancel(true)
            // Наименование потока, выполняющего задачу
            // Наименование потока, выполняющего задачу
            return "" + Thread.currentThread().name
        }
    }
    inner class CallableDelay(cycle: Int = 50): Callable<String> {
        var cycle = cycle
        override fun call(): String {
            while (cycle > 0) {
                System.out.println("" + cycle);
                Thread.sleep(1000);
                cycle--;
            }
            // Останов 2-ой задачи
            futureTask[1].cancel(true);
            // Закрытие серверного сокета
            serverSoket.close();

            Log.d(TAG, "Thread '"
                    + Thread.currentThread().getName()
                    + "' stoped" );
            // Наименование потока, выполняющего задачу
            return "" + Thread.currentThread().getName();
        }

    }
}