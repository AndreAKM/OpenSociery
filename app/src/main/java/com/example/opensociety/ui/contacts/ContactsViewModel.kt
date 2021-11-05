package com.example.opensociety.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext
    var contacts = Contacts(context)
    private val _text = MutableLiveData<String>().apply {
        value = contacts.get_contact(1L)?.
                let{it.getWholeJson().toString()} ?: contacts.getIP()
    }
    val text: LiveData<String> = _text
}