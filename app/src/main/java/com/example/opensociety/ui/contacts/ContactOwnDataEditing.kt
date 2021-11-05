package com.example.opensociety.ui.contacts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.opensociety.R
import com.example.opensociety.connection.CommandFactory
import com.example.opensociety.databinding.FragmentContactOwnDataEditingBinding
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread

class ContactOwnDataEditing : Fragment() {
    val TAG = "ContactOwnDataEditing"

    private lateinit var viewModel: ContactDataViewModel
    private lateinit var _binding: FragmentContactOwnDataEditingBinding
    var id: Long? = null
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
    var today = Calendar.getInstance()
    val firstApiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    //var date = LocalDate.parse("2019-08-07 09:00:00" , firstApiFormat)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val arg =arguments
        Log.d(TAG, "onCreateView($savedInstanceState) {friend: $arg}")
        id = arg!!.getLong(CONTACT_ID, -1L)
        friend = when {
            id == -1L && arg != null -> {
                arg!!.getString(CONTACT_DATA)?.let {
                    Friend(JSONObject(it))
                } ?: Friend()
            }
            id != -1L -> context?.let { c ->
                Contacts(c).get_contact(id!!).also {
                    Log.d(TAG, "ID: $id, friend: ${it?. let{it.getWholeJson().toString()}}")}
            } ?: null
            else -> Friend()
        }

        Log.d(TAG, "friend: $friend")
        _binding =
            FragmentContactOwnDataEditingBinding.inflate(inflater, container,false)

        val today = Calendar.getInstance()
        when {
            (friend != null) -> {
                Log.d(TAG, "Friend(${friend!!.id})'s IP: ${friend!!.ip}")
                _binding.title.text = friend!!.getTitle()
                _binding.firsName.setText(friend!!.first_name)
                _binding.secondName.setText(friend!!.second_name)
                _binding.familyName.setText(friend!!.family_name)
                _binding.birthday.text = friend!!.birthday
                _binding.IP.text = friend!!.ip
                _binding.status.text = (friend!!.status.toString())
            }
            else -> {
                contacts?.getIPAddress().also {
                    Log.d(TAG, "ID: 1 IP: $it")
                }?.let {
                    _binding.IP.setText(it)
                    _binding.status.text = (Friend.Status.OWNER.toString())
                    _binding.birthday.text =
                        "${today.get(Calendar.YEAR)}-${today.get(Calendar.MONTH)}" +
                                "-${today.get(Calendar.DATE)}"
                    friend = Friend(status = Friend.Status.OWNER, ip = it)
                }
            }
        }
        _binding.birthday.setOnClickListener {
            val newFragment: DialogFragment = SelectDateFragment(_binding.birthday)
            newFragment.show(getParentFragmentManager(), "DatePicker")
        }

        _binding.status.isEnabled = false
        _binding.status.isClickable = false
        _binding.btnYes.setOnClickListener{saveData()}
        _binding.btnCancel.setOnClickListener{this.findNavController().
            popBackStack(R.id.navigation_contact_own_data_editing, true)}
        return _binding.root
    }

    fun saveData() {
        val isChanged = friend!!.nick == _binding.nickname.text.toString() ||
            friend!!.first_name == _binding.firsName.text.toString() ||
            friend!!.second_name == _binding.secondName.text.toString() ||
            friend!!.family_name == _binding.familyName.text.toString()
        friend!!.nick = _binding.nickname.text.toString()
        friend!!.first_name = _binding.firsName.text.toString()
        friend!!.second_name = _binding.secondName.text.toString()
        friend!!.family_name = _binding.familyName.text.toString()
        friend!!.ip = _binding.IP.text.toString()
        Log.d(TAG,"Save friend: ${friend!!.getWholeJson()}")
        friend!!.id?. takeIf {it == 1L}?. let { contacts!!.updateContact(friend!!) } ?:
            contacts!!.add_friend(friend!!)
        if (isChanged) {
            thread {
                contacts?.sendToAll(CommandFactory::makeUpdateForm)
            }
        }
        this.findNavController().popBackStack(R.id.navigation_contact_own_data_editing, true)
    }
}