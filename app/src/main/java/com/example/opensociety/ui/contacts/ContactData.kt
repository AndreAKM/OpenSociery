package com.example.opensociety.ui.contacts

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.opensociety.R
import com.example.opensociety.databinding.ContactDataFragmentBinding
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject

class ContactData : Fragment() {
    val TAG = "ContactData"
    companion object {
        fun newInstance(id:Int?=null, json: JSONObject?=null) = ContactData().also { data ->
            Log.d("ContactData", "newInstance")
                when {
                    id != null -> data.arguments = Bundle().also { it.putInt("contact_id", id) }
                    json != null -> data.arguments = Bundle().also {
                        it.putString("contact_data", json.toString())
                    }
                }
            }
    }

    private lateinit var viewModel: ContactDataViewModel
    private lateinit var _binding: ContactDataFragmentBinding
    var id: Int? = null
    var isEditing = false

    private var CONTACT_DATA: String?= null
    private var CONTACT_ID: String? = null

    private var contacts: Contacts? = null
    private var friend:Friend? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate($savedInstanceState) {context: $context}")

        CONTACT_DATA = context?.getString(R.string.contact_data)
        CONTACT_ID = context?.getString(R.string.contact_id)
        contacts = context?. let{ Contacts(it) }

        Log.d(TAG, "Contacts: $contacts")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val arg =arguments
        Log.d(TAG, "onCreateView($savedInstanceState) {friend: $arg}")
        id = arg!!.getInt(CONTACT_ID, -1)
        if (id == -1) {
            friend = arg!!.getString(CONTACT_DATA)?.
            let {Friend(JSONObject(it))} ?:
                    Friend()
        }
        else {
            friend = context?.let {
                Contacts(it).find_contact(JSONObject().put(Friend.ID, id))?.run{this[0]}
            } ?: null
        }
        Log.d(TAG, "friend: $friend")
        _binding = ContactDataFragmentBinding.inflate(inflater, container, false)
        _binding.status.adapter = context?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item, Friend.Status.names())
        }
        if (friend != null) {
            _binding.title.setText(friend!!.getName())
            _binding.firsName.setText(friend!!.first_name)
            _binding.secondName.setText(friend!!.second_name)
            _binding.familyName.setText(friend!!.family_name)
            _binding.status.setSelection(friend!!.status.ordinal)
        }
        _binding.status.isEnabled = false;
        _binding.status.isClickable = false;
        _binding.btnEdit.setOnClickListener { goToEditing() }
        _binding.btnYes.setOnClickListener{saveData()}
        _binding.btnCancel.setOnClickListener{this.findNavController().
            popBackStack(R.id.navigation_contact_data, true)}
        return _binding.root
    }

    fun goToEditing() {
        Log.d(TAG, "goToEditing")
        isEditing = true
        _binding.title.text = context?.getString(R.string.nickname)
        _binding.nickname.visibility = View.VISIBLE
        _binding.nickname.setText(friend!!.nick)
        _binding.firsName.inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
        _binding.secondName.inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
        _binding.familyName.inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
        _binding.btnCancel.visibility = View.VISIBLE
        _binding.btnYes.visibility = View.VISIBLE
        _binding.btnEdit.visibility = View.INVISIBLE
        _binding.status.isEnabled = true;
        _binding.status.isClickable = true;
    }

    fun saveData() {
        friend!!.nick = _binding.nickname.text.toString()
        friend!!.first_name = _binding.firsName.text.toString()
        friend!!.second_name = _binding.secondName.text.toString()
        friend!!.family_name = _binding.familyName.text.toString()
        friend!!.status = Friend.Status.intToStatus(_binding.status.selectedItemId.toInt())
        Log.d(TAG,"Save friend: ${friend!!.getJson()}")
        friend!!.id?. takeIf {it > 0}?. let { contacts!!.updateContact(friend!!) } ?:
            contacts!!.add_friend(friend!!)
        this.findNavController().popBackStack(R.id.navigation_contact_data, true)
    }
}