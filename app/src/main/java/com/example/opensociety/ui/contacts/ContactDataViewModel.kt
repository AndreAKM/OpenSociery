package com.example.opensociety.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject
import kotlin.concurrent.fixedRateTimer

class ContactDataViewModel(application: Application, id:Long) : AndroidViewModel(application) {
    val context = application.applicationContext
    var contacts = Contacts(context)
    var id = id
    private val _friend = MutableLiveData<Friend>().apply {
        value = contacts.get_contact(id)?.let{it} ?: null
    }
    private val friend: LiveData<Friend>  = _friend
}