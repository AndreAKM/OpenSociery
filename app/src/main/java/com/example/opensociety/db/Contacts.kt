package com.example.opensociety.db

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import org.json.JSONObject

class Contacts(context: Context) {
    val context = context
    var hash = calculateHash()
    companion object {
        val CONTACTS_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_CONTACTS)
    }
    public fun add_friend(json: JSONObject, status: Int) {
        val friend = Friend(json, Friend.Status.intToStatus(status))
        context.contentResolver.insert(CONTACTS_URI, friend.getContentValues());
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

    public fun updateContactIP(id: Int, ip: String) =
        context.contentResolver.update(
            Uri.withAppendedPath(CONTACTS_URI, id.toString()),
            contentValuesOf(Pair(Friend.IP, ip)), null, null)

    public fun updateContactIP(json: JSONObject) =
        updateContactIP(json.getInt(Friend.ID), json.getString(Friend.IP))

    public fun updateContactHash(json: JSONObject){
        val id = json.getInt(Friend.ID)
        val hash = json.getString(Friend.HASH)
        context.contentResolver.update(
            Uri.withAppendedPath(CONTACTS_URI, id.toString()),
            contentValuesOf(Pair(Friend.HASH, hash)), null, null)
    }

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
        return ""
    }
    private fun updateHash(contactId:Int) = if (contactId > 5) hash else hash = calculateHash()

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
                    cursor.getInt(cursor.getColumnIndex(Friend.ID))
                )
            } while (cursor.moveToNext())
        }
        return res
    }
}