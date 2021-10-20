package com.example.opensociety.db

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.opensociety.R

class DBContentProvider : ContentProvider() {
    val TAG = "DBContentProvider"

    var uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    companion object {
        val AUTHORITIES = "OpenSociety"
        val BASE_CONTENT_URI = Uri.parse("content://$AUTHORITIES")

        val CONTACTS = 1
        val NEWS = 3
        val CHATS_LIST = 5
        val MESSAGES = 7
        val VIEWERS = 9
        val IP_LIST = 10
        val CONTACTS_ID = 1 + 1
        val NEWS_ID = 3 + 1
        val CHATS_LIST_ID = 5 + 1
        val MESSAGES_ID = 7 + 1
        val VIEWERS_ID = 9 + 1
        val IP_LIST_ID = 10 + 1
    }
    init {
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_CONTACTS, CONTACTS)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_IP_LIST, IP_LIST)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_NEWS, NEWS)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_CHATS_LIST, CHATS_LIST)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_MESSAGES, MESSAGES)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_VIEWERS, VIEWERS)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_CONTACTS + "/#", CONTACTS_ID)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_IP_LIST + "/#", IP_LIST_ID)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_NEWS + "/#", NEWS_ID)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_CHATS_LIST + "/#", CHATS_LIST_ID)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_MESSAGES + "/#", MESSAGES_ID)
        uriMatcher.addURI(AUTHORITIES, DbStructure.TB_VIEWERS + "/#", VIEWERS_ID)
    }
    var dbHelper: DbStructure? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return dbHelper?.writableDatabase?.delete(UriToTbName(uri), selection, selectionArgs) ?: 0
    }

    override fun getType(uri: Uri): String? {
        TODO(
            "Implement this to handle requests for the MIME type of the data" +
                    "at the given URI"
        )
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = dbHelper?.writableDatabase?.insert(UriToTbName(uri), null, values)
            ?: -1
        if (id == -1L) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, id);
    }

    override fun onCreate(): Boolean {
        dbHelper = context?.let { DbStructure(it) }
        return dbHelper != null
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        when(uriMatcher.match(uri)%2) {
            1 -> return dbHelper?.readableDatabase?.query(UriToTbName(uri), projection, selection,
                selectionArgs, null, null, sortOrder) ?: null ?: null
            0 -> {
                val selection = DbStructure.F_ID + "=?";
                val selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                return dbHelper?.readableDatabase?.query(UriToTbName(uri), projection, selection,
                    selectionArgs, null, null, null);
            }
        }
        return null;
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (values?.size() == 0) {
            return 0;
        }
        when(uriMatcher.match(uri)%2) {
            1 -> dbHelper!!.readableDatabase!!.update(UriToTbName(uri), values, selection,
                selectionArgs)
            0 -> {
                val selection = DbStructure.F_ID + "=?";
                val selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                dbHelper!!.readableDatabase!!.update(UriToTbName(uri), values, selection,
                    selectionArgs);
            }
        }
        return 0;
    }

    private fun UriToTbName (uri: Uri):String {
        when(uriMatcher.match(uri)) {
            CONTACTS, CONTACTS_ID -> return DbStructure.TB_CONTACTS
            IP_LIST, IP_LIST_ID -> return DbStructure.TB_IP_LIST
            NEWS, NEWS_ID -> return DbStructure.TB_NEWS
            CHATS_LIST, CHATS_LIST_ID -> return DbStructure.TB_CHATS_LIST
            MESSAGES, MESSAGES_ID -> return DbStructure.TB_MESSAGES
            VIEWERS, VIEWERS_ID -> return DbStructure.TB_VIEWERS
            else -> return ""
        }
    }
}