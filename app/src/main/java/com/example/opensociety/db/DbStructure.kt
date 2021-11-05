package com.example.opensociety.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbStructure(context: Context):
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    val context = context

    companion object {
        const val DB_NAME = "data-base.db"
        const val DB_VERSION = 1;

        val TB_CONTACTS = "contacts"
        val TB_IP_LIST = "ip_list"
        val TB_HASH_LIST = "hash_list"
        val TB_NEWS = "news"
        val TB_CHATS_LIST = "chats_list"
        val TB_MESSAGES = "messages"
        val TB_VIEWERS = "viewers"
        val F_ID = "_id"
        val F_HASH = "hash"
        val F_STATUS = "status"
        val F_IP = "ip"
        val F_IP_ID = "ip_id"
        val F_NICK_NAME = "nick_name"
        val F_FIRST_NAME = "first_name"
        val F_SECOND_NAME = "second_name"
        val F_FAMILY_NAME = "family_name"
        val F_AVATAR = "avatar"
        val F_DATA = "data"
        val F_AUTHOR = "author"
        val F_FRIEND_ID = "friend_id"
        val F_PARENT_ID = "parent_id"
        val F_MESSAGE_ID = "message_id"
        val F_CHAT_HASH = "chat_hash"
        val F_VIEWER_ID = "viewer_id"
        val F_AUTHOR_ID = "author_id"
        val F_CONTACT_ID = "contact_id"
        val F_IS_SYNC = "is_sync"
        val F_CREATIMG_TIME = "creating_time"
        val F_BIRTHDAY = "birthday"

        private val DEFAULT_TEXT = " TEXT DEFAULT \"\" NOT NULL"
        private val DEFAULT_INT = " INTEGER DEFAULT 0 NOT NULL"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL("CREATE TABLE $TB_CONTACTS ($F_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$F_IP $DEFAULT_TEXT, $F_NICK_NAME $DEFAULT_TEXT, $F_FIRST_NAME $DEFAULT_TEXT,"+
                    " $F_SECOND_NAME $DEFAULT_TEXT, $F_FAMILY_NAME $DEFAULT_TEXT, $F_AVATAR " +
                    "$DEFAULT_TEXT, $F_BIRTHDAY TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    " $F_HASH INTEGER DEFAULT 0 NOT NULL UNIQUE," +
                    " $F_STATUS TEXT DEFAULT \"UNKNOWN\" NOT NULL, $F_CREATIMG_TIME TIMESTAMP" +
                    " DEFAULT CURRENT_TIMESTAMP NOT NULL );")
            db.execSQL("CREATE TABLE $TB_IP_LIST ($F_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$F_IP $DEFAULT_TEXT, $F_CONTACT_ID $DEFAULT_INT," +
                    " UNIQUE ($F_IP, $F_CONTACT_ID));");
            db.execSQL("CREATE TABLE $TB_HASH_LIST ($F_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$F_HASH INTEGER NOT NULL, $F_CONTACT_ID INTEGER NOT NULL," +
                    " UNIQUE ($F_HASH, $F_CONTACT_ID));");
            db.execSQL("CREATE TABLE $TB_NEWS ($F_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$F_MESSAGE_ID INTEGER);");
            db.execSQL("CREATE TABLE $TB_CHATS_LIST ($F_ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "$F_PARENT_ID DEFAULT_INT, $F_HASH INTEGER UNIQUE, $F_FRIEND_ID $DEFAULT_TEXT);");
            db.execSQL("CREATE TABLE $TB_MESSAGES ($F_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$F_AUTHOR_ID DEFAULT_INT, $F_IS_SYNC DEFAULT_TEXT, $F_DATA DEFAULT_TEXT, " +
                    "$F_CREATIMG_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);");
            db.execSQL("CREATE TABLE $TB_VIEWERS ($F_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$F_MESSAGE_ID DEFAULT_INT, $F_FRIEND_ID DEFAULT_INT," +
                    " UNIQUE ($F_MESSAGE_ID, $F_FRIEND_ID));")
            //db.execSQL("INSERT INTO $TB_CONTACTS ($F_FAMILY_NAME) VALUES (\"YOUR CONTACT INFORMATION\");")
        }

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}