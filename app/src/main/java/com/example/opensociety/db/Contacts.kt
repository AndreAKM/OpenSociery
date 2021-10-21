package com.example.opensociety.db

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import org.json.JSONObject
import java.lang.Exception
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class Contacts(context: Context) {
    val context = context
    var hash = calculateHash()
    companion object {
        val CONTACTS_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_CONTACTS)
        val IP_LIST_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_IP_LIST)
        val HASH_LIST_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_HASH_LIST)
    }
    public fun add_friend(json: JSONObject, status: Int) {
        val friend = Friend(json, Friend.Status.intToStatus(status))
        val id = context.contentResolver.insert(CONTACTS_URI, friend.getContentValues())?.let {
            ContentUris.parseId(it)
        } ?: return
        updateHash(id)
    }

    public fun add_friend(json: JSONObject) = add_friend(json, json.getInt(Friend.STATUS))

    public fun get_contacts(json: JSONObject): Array<Friend> {
        var result = emptyArray<Friend>()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun find_contact(json: JSONObject): Array<Friend>? {
        var select = ""
        var selectArgs = emptyArray<String>()
        for( i in json.keys()) {
            select +=  if (select.isEmpty()) {"? = "} else {" AND ? = "} + i
            selectArgs += json.getString(i)
        }
        var cursor = context.contentResolver?.query(
            CONTACTS_URI, null, select, selectArgs,
            Friend.NICK + ", " + Friend.FAMILY_NAME + ", " + Friend.SECOND_NAME + ", " +
                Friend.FAMILY_NAME
        )
        return cursor?.let { cursorToContack(it) } ?: null
    }

    public fun updateContactIP(id: Long, ip: String) =
        context.contentResolver. also {
            it.update(
                Uri.withAppendedPath(CONTACTS_URI, id.toString()),
                contentValuesOf(Pair(Friend.IP, ip)), null, null)
        }.insert(IP_LIST_URI,
            contentValuesOf(Pair(Friend.IP, ip), Pair(DbStructure.F_CONTACT_ID, id)))

    public fun updateContactIP(json: JSONObject) =
        updateContactIP(json.getLong(Friend.ID), json.getString(Friend.IP))

    public fun updateContactHash(json: JSONObject) {
        val id = json.getLong(Friend.ID)
        val hash = json.getInt(Friend.HASH)
        updateContactHash(id, hash)
    }

    public fun updateContactHash(id: Long, hash: Int) = context.contentResolver.also{it.update(
            Uri.withAppendedPath(CONTACTS_URI, id.toString()),
            contentValuesOf(Pair(Friend.HASH, hash)), null, null)}
        .insert(HASH_LIST_URI,
            contentValuesOf(Pair(Friend.HASH, hash), Pair(DbStructure.F_CONTACT_ID, id)))


    private fun calculateHash(): Int {
        var cursor = context.contentResolver.query(
            CONTACTS_URI,
            arrayOf(Friend.NICK, Friend.FAMILY_NAME, Friend.SECOND_NAME, Friend.FAMILY_NAME),
            Friend.ID + " BETWEEN 1 AND 5",
            null,
            Friend.ID
        )
        return cursor?.let {
            var accum = ""
            if (cursor.moveToFirst()) {
                do {
                    accum += it.getString(it.getColumnIndex(Friend.NICK)) +
                            it.getString(it.getColumnIndex(Friend.FIRST_NAME)) +
                            it.getString(it.getColumnIndex(Friend.SECOND_NAME)) +
                            it.getString(it.getColumnIndex(Friend.FAMILY_NAME))
                } while (it.moveToNext())
            }
            accum.hashCode()
        } ?: 0
    }

    public fun getIP(): String {
        var cursor = context.contentResolver.query(
            Uri.withAppendedPath(CONTACTS_URI, "1"),
            arrayOf(Friend.IP), null, null, null)
        return cursor?.let {
            if (cursor.moveToFirst()) {
                it.getString(it.getColumnIndex(Friend.IP))
            } else {
                updateIP()
            }
        } ?: updateIP()
    }

    public fun updateIP(): String {
        var ip = getIPAddress()
        if (getIP() != ip) updateContactIP(1, ip)
        return ip
    }

    private fun updateHash(contactId:Long) = if (contactId > 5) hash else {
            hash = calculateHash()
            updateContactHash(1, hash)
        }

    private fun cursorToContack(cursor: Cursor): Array<Friend>? {
        var res : Array<Friend>? = null
        if (cursor.moveToFirst()) {
            res = emptyArray()
            do {
                res += Friend(
                    cursor.getString(cursor.getColumnIndex(Friend.IP)),
                    cursor.getString(cursor.getColumnIndex(Friend.NICK)),
                    cursor.getString(cursor.getColumnIndex(Friend.FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.SECOND_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.FAMILY_NAME)),
                    cursor.getString(cursor.getColumnIndex(Friend.AVATAR)),
                    Friend.Status.valueOf(cursor.getString(cursor.getColumnIndex(Friend.STATUS))),
                    cursor.getInt(cursor.getColumnIndex(Friend.HASH)),
                    cursor.getLong(cursor.getColumnIndex(Friend.ID))
                )
            } while (cursor.moveToNext())
        }
        return res
    }

    fun getIPAddress(): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.getInetAddresses())
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress() ) {
                        val sAddr: String = addr.getHostAddress()
                        if (addr is Inet4Address) {
                            return sAddr
                        } else {
                            if (addr is Inet6Address) {
                                val delim = sAddr.indexOf('%') // drop ip6 port suffix
                                return if (delim < 0) sAddr else sAddr.substring(0, delim)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}