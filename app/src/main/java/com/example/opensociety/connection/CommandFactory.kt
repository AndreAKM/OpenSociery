package com.example.opensociety.connection

import android.content.Context
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject

class CommandFactory {
    companion object {
        fun makeAskingAccess(distination: Friend, owner: Friend): JSONObject {
            var jsonObject = JSONObject()
            jsonObject.put(ConnectionWorker.COMMAND, ConnectionWorker.ACCESS_REQUEST)
            owner!!.status = distination.status
            jsonObject.put(ConnectionWorker.DATA, owner!!.getJson())
            return jsonObject
        }

        fun makeChangeAccessStatus(distination: Friend, owner: Friend): JSONObject {
            var jsonObject = JSONObject()
            jsonObject.put(ConnectionWorker.COMMAND, ConnectionWorker.CHANGE_ACCESS_STATUS)
            owner!!.status = distination.status
            jsonObject.put(ConnectionWorker.DATA, owner!!.getJson())
            return jsonObject
        }
    }
}