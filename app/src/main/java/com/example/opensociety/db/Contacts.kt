package com.example.opensociety.db

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.contentValuesOf
import com.example.opensociety.connection.CommandFactory
import com.example.opensociety.connection.Connection
import com.example.opensociety.connection.Server
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class Contacts(context: Context) {
    val TAG = "Contacts"
    val context = context
    var hash = getHash() .takeIf { it != 0L }?. let{it} ?: calculateHash()

    companion object {
        val CONTACTS_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_CONTACTS)
        val IP_LIST_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_IP_LIST)
        val HASH_LIST_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_HASH_LIST)
    }

    public fun add_friend(friend: Friend): Long? =
        find_contacts(JSONObject().put(Friend.HASH, hash))?.let {
            context.contentResolver.update(
                CONTACTS_URI,
                friend.getContentValues(), "${Friend.ID} = ${it[0].id}", null
            )
            it[0].id
        } ?: context.contentResolver.insert(CONTACTS_URI, friend.getContentValues())?.let {
            val id = ContentUris.parseId(it)
            if (id == 1L) {
                context.contentResolver.update(
                    CONTACTS_URI,
                    contentValuesOf(Pair(Friend.HASH, calculateHash())),
                    "${Friend.ID} = $id", null)
            }
            id
        }


    public fun add_friend(json: JSONObject, status: String): Long? {
        val friend = Friend(json, Friend.Status.valueOf(status))
        if (find_contacts(JSONObject().put(Friend.NICK, friend.nick)
                .put(Friend.FIRST_NAME, friend.first_name)
                .put(Friend.SECOND_NAME, friend.second_name)
                .put(Friend.FAMILY_NAME, friend.family_name)) != null) {
            friend.status = Friend.Status.DUPLICATE
        }
        return add_friend(friend)
    }

    public fun add_friend(json: JSONObject) = add_friend(json, json.getString(Friend.STATUS))

    public  fun get_contact(id:Long): Friend? {
        var cursor = context.contentResolver.query(
            Uri.withAppendedPath(CONTACTS_URI, "1"),
            null, null, null, null)
        return cursor?.let { cursorToContack(it) } ?: null
    }

    public fun find_contacts(json: JSONObject): Array<Friend>? {
        var select = ""
        var selectArgs = emptyArray<String>()
        for( i in json.keys()) {
            json.getString(i). takeIf { it.isNotEmpty() }?. let {
                select += if (select.isEmpty()) {
                    "$i = ?"
                } else {
                    " AND $i = ?"
                }
                selectArgs += it
            }
        }
        Log.d(TAG, "find contact for select: ($select) values:(${selectArgs.toString()})");
        var cursor = context.contentResolver?.query(
            CONTACTS_URI, null, select, selectArgs,
            "${Friend.NICK}, ${Friend.FIRST_NAME}, ${Friend.SECOND_NAME}," +
                    " ${Friend.FAMILY_NAME}" // GROUP BY (${Friend.STATUS})
        )
        return cursor?.let { cursorToContacksArray(it) } ?: null
    }

    fun contactID(contact_hash:Long):Long? {
        var cursor = context.contentResolver?.query(
            Contacts.CONTACTS_URI, arrayOf(DbStructure.F_ID), Friend.HASH + " = ?",
            arrayOf(contact_hash.toString()), null)
        return cursor?.let { it.takeIf { it.moveToFirst()}?.let{it.getLong(0)} } ?: null
    }

    public fun updateContactIP(id: Long, ip: String) =
        context.contentResolver. also {
            it.update(
                Uri.withAppendedPath(CONTACTS_URI, id.toString()),
                contentValuesOf(Pair(Friend.IP, ip)), null, null)
        }.let{ Log.d(TAG, "insert ( ${Friend.IP}: $ip, ${DbStructure.F_CONTACT_ID}: $id)")
            var cursor = it.query(IP_LIST_URI, null,
                "${Friend.IP} = ? AND ${DbStructure.F_CONTACT_ID} = ? ",
                arrayOf(ip, id.toString()), null)
            if (!(cursor != null && cursor.getCount() > 0) ) it.insert(IP_LIST_URI,
                contentValuesOf(Pair(Friend.IP, ip), Pair(DbStructure.F_CONTACT_ID, id)))
        }

    public fun updateContact(friend:Friend) =
        context.contentResolver. also {
            it.update(
                Uri.withAppendedPath(CONTACTS_URI, friend.id.toString()),
                friend.getContentValues(), null, null)
            //TODO if it is a duplicate of the contact add the new hash in the hash list
        }

    public fun updateContactIP(json: JSONObject) =
        find_contacts(JSONObject().put(Friend.HASH,json.getLong(Friend.HASH)))?.
            get(0)?.id?.let { updateContactIP(it, json.getString(Friend.IP)) }

    public fun updateContactHash(json: JSONObject) {
        val id = json.getLong(Friend.ID)
        val hash = json.getInt(Friend.HASH)
        updateContactHash(id, hash)
    }

    public fun updateContactHash(id: Long, hash: Int) = context.contentResolver.also{it.update(
            Uri.withAppendedPath(CONTACTS_URI, id.toString()),
            contentValuesOf(Pair(Friend.HASH, hash)), null, null)}
        .let{ Log.d(TAG, "insert ( ${Friend.HASH}: $hash, ${DbStructure.F_CONTACT_ID}: $id)")
            var cursor = it.query(HASH_LIST_URI, null,
                "${Friend.HASH} = ? AND ${DbStructure.F_CONTACT_ID} = ? ",
                arrayOf(hash.toString(), id.toString()), null)
            if (!(cursor != null && cursor.getCount() > 0) ) it.insert(
                HASH_LIST_URI,
                contentValuesOf(Pair(Friend.HASH, hash), Pair(DbStructure.F_CONTACT_ID, id))
            )
        }


    private fun calculateHash(): Int {
        var cursor = context.contentResolver.query(
            CONTACTS_URI,
            arrayOf(Friend.IP, Friend.CREATING_TIME),
            "${Friend.ID} = 1",
            null,
            Friend.ID
        )
        return cursor?.let {
            var accum = ""
            if (cursor.moveToFirst()) {
                do {
                    for(i in 0 until it.columnCount) {
                        accum += it.getString(i)
                    }
                } while (it.moveToNext())
            }
            accum.hashCode()
        } ?: 0
    }

    public fun getIP(): String? {
        var cursor = context.contentResolver.query(
            Uri.withAppendedPath(CONTACTS_URI, "1"),
            arrayOf(Friend.IP), null, null, null)
        return cursor?.let {
            it.takeIf{it.moveToFirst()}?. getString(it.getColumnIndex(Friend.IP))
        }
    }

    public fun getHash(): Long {
        var cursor = context.contentResolver.query(
            Uri.withAppendedPath(CONTACTS_URI, "1"),
            arrayOf(Friend.HASH), null, null, null)
        return cursor?.let {
            it.takeIf{it.moveToFirst()}?. getLong(it.getColumnIndex(Friend.HASH))
        } ?: 0L
    }

    public fun sendToAll(makeCommand: (d: Friend, s: Friend) -> JSONObject) {
        Log.d(TAG, "send to all")
        if(isNetworkAvailable(context)) contactsList().takeIf { it.isNotEmpty() }?. let {
            var owner: Friend? = null
            Log.d(TAG, "size of contact list: ${it.size}")
            for (c in it) {
                if (c.status == Friend.Status.OWNER) {
                    owner = c
                    Log.d(TAG, "owner is ${owner.getWholeJson()}")
                } else if (owner != null){
                    Log.d(TAG, "Send Updated IP(${owner.ip}) to ${c.getTitle()}: ${c.ip}")
                    GlobalScope.launch {
                        val command =
                            makeCommand(c, owner!!)
                        try {
                            Log.d(TAG, "create connection")
                            var connection = Connection(c!!.ip, c!!.port)
                            Log.d(TAG, "Openning connection")
                            connection.openConnection()
                            Log.d(TAG, "Sending data")
                            connection.sendData(command.toString())
                            Log.d(TAG, "finish sending")
                        } catch (e: Exception) {
                            Log.e(TAG, "can not send $command to ${c.getTitle()}: " +
                                    "${c.ip}:${c.port}. the reason: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    public fun updateIP(): String {
        var ip = Server.addresInString()
        if (getIP() != ip) {
            Log.d(TAG, "updateresult: " + updateContactIP(1, ip!!))
            sendToAll(CommandFactory::makeChangeIp)
        }
        Log.d(TAG, "updateIP()->" + ip);
        return ip!!
    }

    private fun cursorToContack(cursor: Cursor) = cursor.takeIf { it.moveToFirst() }?. let {
                Friend(
                    cursor.getString(cursor.getColumnIndex(Friend.IP)),
                    cursor.getInt(cursor.getColumnIndex(Friend.PORT)),
                    cursor.getString(cursor.getColumnIndex(Friend.NICK)),
                    cursor.getString(cursor.getColumnIndex(Friend.FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.SECOND_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.FAMILY_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.BIRTHDAY)),
                    cursor.getString(cursor.getColumnIndex(Friend.AVATAR)),
                    Friend.Status.valueOf(cursor.getString(cursor.getColumnIndex(Friend.STATUS))),
                    cursor.getLong(cursor.getColumnIndex(Friend.HASH)),
                    cursor.getLong(cursor.getColumnIndex(Friend.ID)),
                    cursor.getString(cursor.getColumnIndex(Friend.CREATING_TIME))
                )
        } ?: null

    private fun cursorToContacksArray(cursor: Cursor): Array<Friend>? {
        var res : Array<Friend>? = null
        if (cursor.moveToFirst()) {
            res = emptyArray()
            do {
                res += Friend(
                    cursor.getString(cursor.getColumnIndex(Friend.IP)),
                    cursor.getInt(cursor.getColumnIndex(Friend.PORT)),
                    cursor.getString(cursor.getColumnIndex(Friend.NICK)),
                    cursor.getString(cursor.getColumnIndex(Friend.FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.SECOND_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.FAMILY_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.BIRTHDAY)),
                    cursor.getString(cursor.getColumnIndex(Friend.AVATAR)),
                    Friend.Status.valueOf(cursor.getString(cursor.getColumnIndex(Friend.STATUS))),
                    cursor.getLong(cursor.getColumnIndex(Friend.HASH)),
                    cursor.getLong(cursor.getColumnIndex(Friend.ID)),
                    cursor.getString(cursor.getColumnIndex(Friend.CREATING_TIME))
                ). also { Log.d(TAG, "find friend ${it.getWholeJson()}") }
            } while (cursor.moveToNext())
        }
        return res
    }

    fun contactsAlphavitList(): Array<Friend> {
        var cursor = context.contentResolver?.query(
            CONTACTS_URI, null, null, null,
            Friend.STATUS + ", " + Friend.NICK + ", " + Friend.FAMILY_NAME + ", " +
                Friend.SECOND_NAME + ", " + Friend.FAMILY_NAME
        )
        return cursor?.let { cursorToContacksArray(it) } ?: emptyArray()
    }

    fun contactsList(): Array<Friend> {
        var cursor = context.contentResolver?.query(
            CONTACTS_URI, null, null, null, Friend.ID
        )
        return cursor?.let { cursorToContacksArray(it) } ?: emptyArray()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.d (TAG, "connect via TRANSPORT_WIFI")
                    true
                }
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.d(TAG, "connect via TRANSPORT_CELLULAR")
                    true
                }
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.d(TAG, "connect via TRANSPORT_ETHERNET")
                    true
                }
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                    Log.d(TAG, "connect via TRANSPORT_BLUETOOTH")
                    true
                }
                else -> {
                    Log.d(TAG, "the devise if offline")
                    false
                }
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }
}