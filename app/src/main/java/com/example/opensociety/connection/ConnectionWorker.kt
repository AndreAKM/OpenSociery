package com.example.opensociety.connection

import android.content.Context
import android.util.Log
import com.example.opensociety.db.ChatManager
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject
import java.io.*
import java.net.Socket

class ConnectionWorker(socket: Socket, context: Context) {
    private val TAG = "ConnectionWorker"
    private val socket = socket
    private val context = context
    companion object {
        const val COMMAND = "command"
        const val ACCESS_REQUEST = "access_request"
        const val CHANGE_ACCESS_STATUS = "access_answer"
        const val MESSAGE = "message"
        const val HASH = "hash"
        const val IP = "ip"
        const val GET_CONTATS = "get_cntacts_list"
        const val FIND_FRIEND = "find_friend"
        const val GET_HASH = "get_hash"
        const val DATA = "data"
    }

    suspend fun run() {
        try {
            var reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                true)
            while (!socket.isClosed) {
                Log.d(TAG, "start")
                val entry = reader.readLine()
                Log.d(TAG, "gets $entry")
                val outData = inputProcess(entry)
                Log.d(TAG, "ConnectionWorker outData: $outData")
                if (outData.isNotEmpty()) {
                    Log.d(TAG, "ConnectionWorker outData: $outData")
                    writer.write(outData)
                    writer.flush()
                }
                Log.d(TAG, "ConnectionWorker outData: $outData")
                socket.close()
            }
            Log.d(TAG, "ConnectionWorker stoped")
        } catch (e:IOException) {
            e.message?.let { Log.e(TAG, it) }
        }
    }
    var contacts = Contacts(context)
    var chat_m = ChatManager(context)
    fun inputProcess(data: String): String {
        val json = JSONObject(data)
        Log.d(TAG, "iputProces($data)")
        var result = ""
        when (json.getString(COMMAND)) {
            ACCESS_REQUEST -> contacts.add_friend(json.getJSONObject(DATA),
                Friend.Status.APPLIED.toString())
            CHANGE_ACCESS_STATUS -> contacts.add_friend(json)
            MESSAGE -> chat_m.registrationInputMessage(json)
            HASH -> contacts.updateContactHash(json.getJSONObject(DATA))
            IP -> contacts.updateContactIP(json.getJSONObject(DATA))
            GET_CONTATS -> result = contacts.get_contacts(json.getJSONObject(DATA)).toString()
            GET_HASH -> {
                var rj = JSONObject()
                rj.put(COMMAND, HASH)
                rj.put(DATA, JSONObject().put(HASH,contacts.hash)
                    .put(IP, contacts.getIP()))
                result = rj.toString()
            }
            FIND_FRIEND -> result = contacts.find_contact(json.getJSONObject(DATA)).toString()
        }
        return result
    }
}