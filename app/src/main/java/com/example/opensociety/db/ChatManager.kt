package com.example.opensociety.db

import android.content.Context
import android.net.Uri
import org.json.JSONObject

class ChatManager(context: Context) {
    val context = context
    val contacts = Contacts(context)
    companion object {
        val CHAT_LIST_URI: Uri =
            Uri.withAppendedPath(DBContentProvider.BASE_CONTENT_URI, DbStructure.TB_CHATS_LIST)

        val AUTHOR_HASH = "author_hash"
        val MESSAGE = "massage"
        val CHAT_HASH = "chat_hash"
    }

    fun registrationInputMessage(json: JSONObject) {
        val chat_id = chatID(json.getLong(CHAT_HASH))
        var author_id = contacts.contactID(json.getLong(AUTHOR_HASH))
        if (author_id == null) {
            author_id = contacts.add_friend(
                Friend(hash = json.getLong(AUTHOR_HASH), status = Friend.Status.UNKNOWN))
        }
        val message = author_id?.let { Message(json.getJSONObject(MESSAGE), true, it) }

    }

    fun chatID(chat_hash:Long):Long? {
        var cursor = context.contentResolver?.query(
            CHAT_LIST_URI, arrayOf(DbStructure.F_ID), Chat.CHAT_HASH + " = ?",
            arrayOf(chat_hash.toString()), null)
        return cursor?.let { it.takeIf { it.moveToFirst()}?.let{it.getLong(0)} } ?: null
    }
}