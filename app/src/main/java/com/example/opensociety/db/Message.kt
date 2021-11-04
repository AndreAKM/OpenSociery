package com.example.opensociety.db

import org.json.JSONObject
import com.example.opensociety.db.Friend

class Message(author_id: Long, is_sync: Boolean, data: String, time: String? = null) {

    var id: Int? = null
    var author_id = author_id
    var is_sync = is_sync
    var body = data
    var time = time

    companion object {
        val ID = DbStructure.F_ID
        val AUTHOR_ID = DbStructure.F_AUTHOR_ID
        val IS_SYNC = DbStructure.F_IS_SYNC
        val DATA = DbStructure.F_DATA
    }


    constructor(json: JSONObject, is_sync: Boolean, author_id: Long):
        this(author_id, is_sync, json.getString(DATA) ) {}

}