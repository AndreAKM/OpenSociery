package com.example.opensociety.ui.contacts

import android.app.Application
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext
    var contacts = Contacts(context)
    private val _text = MutableLiveData<String>().apply {
        value = contacts.find_contact(JSONObject().put(Friend.ID, 1))?.
                let{it[0].getJson().toString()} ?: contacts.getIP()
    }
    val text: LiveData<String> = _text
}