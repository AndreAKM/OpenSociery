package com.example.opensociety.connection

import android.content.Context
import android.util.Log
import com.example.opensociety.db.ChatManager
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import com.example.opensociety.db.Message
import org.json.JSONObject
import java.io.*
import java.net.Socket

class ConnectionWorker(socket: Socket, context: Context): Runnable {
    private val TAG = "ConnectionWorker"
    private val socket = socket
    private val context = context
    companion object {
        const val COMMAND = "command"
        const val ACCESS_REQUEST = "access_request"
        const val ACCESS_ANSWER = "access_answer"
        const val MESSAGE = "message"
        const val HASH = "hash"
        const val IP = "ip"
        const val GET_CONTATS = "get_cntacts_list"
        const val FIND_FRIEND = "find_friend"
        const val GET_HASH = "get_hash"
        const val DATA = "data"
    }
    override fun run() {
        try {
            var inStream = DataInputStream(socket.getInputStream())
            var outStream = DataOutputStream(socket.getOutputStream())
            while (!socket.isClosed) {
                Log.d(TAG, "ConnectionWorker start")
                val entry = inStream.readUTF()
                val outData = inputProcess(entry)
                if (outData.length != 0) {
                    outStream.writeUTF(outData)
                    outStream.flush()
                } else {
                    socket.close()
                    outStream.flush()
                }
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
        var result = ""
        when (json.getString(COMMAND)) {
            ACCESS_REQUEST -> contacts.add_friend(json.getJSONObject(DATA),
                Friend.Status.APPLIED.ordinal)
            ACCESS_ANSWER -> contacts.add_friend(json)
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