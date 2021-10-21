package com.example.opensociety.db

import android.content.ContentValues
import androidx.core.content.contentValuesOf
import org.json.JSONObject

class Friend(ip: String = "", nick: String = "",
             first_name: String = "", second_name: String = "",
             family_name: String = "", avatar: String = "",
             status: Status = Status.APPLIED, hash:Int, id: Long? ) {

    var id = id
    var ip = ip
    var nick = nick
    var first_name = first_name
    var second_name = second_name
    var family_name = family_name
    var avatar = avatar
    var status = status
    var hash = hash

    enum class Status {
        APPLIED, CLOSED, FRIEND, VIEWER, DUBLICATE;
        companion object {
            fun intToStatus(value: Int) = when (value) {
                1 -> APPLIED
                2 -> CLOSED
                3 -> FRIEND
                4 -> VIEWER
                else -> DUBLICATE
            }
        }
    }

    companion object {
        val ID = DbStructure.F_ID
        val IP = DbStructure.F_IP
        val NICK = DbStructure.F_NICK_NAME
        val FIRST_NAME = DbStructure.F_FIRST_NAME
        val SECOND_NAME = DbStructure.F_SECOND_NAME
        val FAMILY_NAME = DbStructure.F_FAMILY_NAME
        val AVATAR = DbStructure.F_AVATAR
        val STATUS = DbStructure.F_STATUS
        val HASH = DbStructure.F_HASH
        val COLUMNS = arrayOf(
            ID,
            IP,
            NICK,
            FIRST_NAME,
            SECOND_NAME,
            FAMILY_NAME,
            AVATAR,
            STATUS,
            HASH
        )
    }

    constructor(jsonObject: JSONObject, status: Status) : this(jsonObject.getString(IP),
        jsonObject.getString(NICK), jsonObject.getString(FIRST_NAME), jsonObject.getString(SECOND_NAME),
        jsonObject.getString(FAMILY_NAME), jsonObject.getString(AVATAR), status,
        jsonObject.getInt(HASH),null) {
        if (jsonObject.has(ID)) {
            id = jsonObject.getLong(ID)
        }
    }

    public fun getContentValues(): ContentValues {
        var r = contentValuesOf(
            Pair(IP, ip),
            Pair(NICK, nick),
            Pair(FIRST_NAME, first_name),
            Pair(SECOND_NAME,second_name),
            Pair(FAMILY_NAME, family_name),
            Pair(AVATAR, avatar),
            Pair(STATUS, status),
            Pair(HASH, hash)
        )
        if(id != null) { r.put(ID,id) }
        return r
    }
}