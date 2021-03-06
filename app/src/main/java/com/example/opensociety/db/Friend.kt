package com.example.opensociety.db

import android.content.ContentValues
import androidx.core.content.contentValuesOf
import org.json.JSONObject
import java.util.*

class Friend(ip: String = "", port: Int = 0, nick: String = "",
             first_name: String = "", second_name: String = "",
             family_name: String = "", birthday:String = "", avatar: String = "",
             status: Status = Status.APPLIED, hash:Long = 0, id: Long? = null,
             create_time: String? = null) {

    var id: Long? = id
    var ip = ip
    var port = port
    var nick = nick
    var first_name = first_name
    var second_name = second_name
    var family_name = family_name
    var birthday = birthday
    var avatar = avatar
    var status = status
    var hash = hash
    val calendar = Calendar.getInstance()
    var create_time: String? = when {
        create_time == null ->
            //YYYY-MM-DD HH:MM:SS
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}" +
                "-${calendar.get(Calendar.DATE)} ${calendar.get(Calendar.HOUR_OF_DAY)}:" +
                "${calendar.get(Calendar.MINUTE)}:${calendar.get(Calendar.SECOND)}"
        else -> create_time
        }


    enum class Status {
        OWNER,
        APPLIED,    // The contacts asked to be friend not answered yet
        CLOSED,     // The contacts are not allowed to look at our activity
        FRIEND,     // The contacts have the whole access to our date
        VIEWER,     // The contacts see only our publications
        REMOUTE,    // There are some publication by the contacts were met
        SUBSCRIPTION,// We collect only publications by the contacts
        STEP_FRIEND, // It is friend of my friend
        DUPLICATE,  // New contacts which have the same names. Possible it is a reconnection
        UNKNOWN;    // We know only hash the other information is being looked for.
        companion object {
            fun intToStatus(value: Int) = Status.values()[value]
            fun names() = Status.values().map { it.name }
        }
    }

    companion object {
        val ID = DbStructure.F_ID
        val IP = DbStructure.F_IP
        val PORT = DbStructure.F_PORT
        val NICK = DbStructure.F_NICK_NAME
        val FIRST_NAME = DbStructure.F_FIRST_NAME
        val SECOND_NAME = DbStructure.F_SECOND_NAME
        val FAMILY_NAME = DbStructure.F_FAMILY_NAME
        val BIRTHDAY = DbStructure.F_BIRTHDAY
        val AVATAR = DbStructure.F_AVATAR
        val STATUS = DbStructure.F_STATUS
        val HASH = DbStructure.F_HASH
        val CREATING_TIME = DbStructure.F_CREATIMG_TIME

        val COLUMNS = arrayOf(
            ID,
            IP,
            PORT,
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
        jsonObject.getInt(PORT), jsonObject.getString(NICK), jsonObject.getString(FIRST_NAME),
        jsonObject.getString(SECOND_NAME), jsonObject.getString(FAMILY_NAME),
        jsonObject.getString(BIRTHDAY), jsonObject.getString(AVATAR), status,
        jsonObject.getLong(HASH)) {
        if(jsonObject.has(ID))id = jsonObject.getLong(ID)
        if(jsonObject.has(CREATING_TIME)) create_time = jsonObject.getString(CREATING_TIME)
    }

    constructor(jsonObject: JSONObject):
            this(jsonObject, Status.valueOf(jsonObject.getString(STATUS))) {

    }

    public fun getTitle() = when {
        nick.isNotEmpty() -> nick
        first_name.isNotEmpty() -> first_name
        family_name.isNotEmpty() -> family_name
        second_name.isNotEmpty() -> second_name
        else -> hash.toString() + ": " + ip
    }

    public fun getContentValues(): ContentValues {
        var r = contentValuesOf(
            Pair(IP, ip),
            Pair(PORT, port),
            Pair(NICK, nick),
            Pair(FIRST_NAME, first_name),
            Pair(SECOND_NAME,second_name),
            Pair(FAMILY_NAME, family_name),
            Pair(BIRTHDAY, birthday),
            Pair(AVATAR, avatar),
            Pair(STATUS, status.name),
            Pair(HASH, hash)
        )
        if(id != null) { r.put(ID,id) }
        return r
    }

    public fun getExportJson() = JSONObject()
            .put(IP, ip)
            .put(PORT, port)
            .put(NICK, nick)
            .put(FIRST_NAME, first_name)
            .put(SECOND_NAME,second_name)
            .put(FAMILY_NAME, family_name)
            .put(BIRTHDAY, birthday)
            .put(AVATAR, avatar)
            .put(STATUS, status)
            .put(HASH, hash)

    public fun getWholeJson() = JSONObject()
        .put(IP, ip)
        .put(PORT, port)
        .put(NICK, nick)
        .put(FIRST_NAME, first_name)
        .put(SECOND_NAME,second_name)
        .put(FAMILY_NAME, family_name)
        .put(BIRTHDAY, birthday)
        .put(AVATAR, avatar)
        .put(STATUS, status)
        .put(HASH, hash)
        .put(ID, id)
        .put(CREATING_TIME, create_time)

}