package com.example.opensociety.ui.contacts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.opensociety.R
import com.example.opensociety.connection.CommandFactory
import com.example.opensociety.connection.Connection
import com.example.opensociety.databinding.FragmentContactDataBinding
import com.example.opensociety.databinding.FragmentContactOtherDataEditingBinding
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject
import kotlin.concurrent.thread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactOtherDataEditingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactOtherDataEditingFragment : Fragment() {
    val TAG = "ContactOtherDataEditingFragment"
    private var CONTACT_DATA: String?= null
    private var CONTACT_ID: String? = null

    private lateinit var _binding: FragmentContactOtherDataEditingBinding
    private var contacts: Contacts? = null
    private var friend: Friend? = null
    var id: Long? = null

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
        // Inflate the layout for this fragment
        _binding = FragmentContactOtherDataEditingBinding.inflate(inflater, container, false)
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
                    Log.d(TAG, "ID: $id, friend: ${it?. let{it.getJson().toString()}}")}
            } ?: null
            else -> Friend()
        }

        Log.d(TAG, "friend: $friend")
        _binding.status.adapter = context?.let {
            ArrayAdapter<String>(it, android.R.layout.simple_spinner_item, Friend.Status.names())
        }
        when {
            (friend != null) -> {
                Log.d(TAG, "Friend(${friend!!.id})'s IP: ${friend!!.ip}")
                _binding.title.text = friend!!.getTitle()
                _binding.firsName.text = friend!!.first_name
                _binding.secondName.text = friend!!.second_name
                _binding.familyName.text = friend!!.family_name
                _binding.birthday.text = friend!!.birthday
                _binding.IP.setText(friend!!.ip)
                _binding.status.setSelection(friend!!.status.ordinal)
            }
            id == 1L -> contacts?.getIPAddress(true). also {
                Log.d (TAG, "ID: 1 IP: $it") } .let{ _binding.IP.setText(it)
                _binding.status.setSelection(Friend.Status.OWNER.ordinal)
            }
        }
        _binding.firsName.isFocusable = false
        _binding.firsName.isClickable = false
        _binding.status.isEnabled = false
        _binding.status.isClickable = false
        _binding.btnYes.setOnClickListener{saveData()}
        _binding.btnCancel.setOnClickListener{this.findNavController().
        popBackStack(R.id.navigation_contact_own_data_editing, true)}
        return _binding.root
    }

    fun saveData() {
        friend!!.nick = _binding.nickname.text.toString()
        friend!!.first_name = _binding.firsName.text.toString()
        friend!!.second_name = _binding.secondName.text.toString()
        friend!!.family_name = _binding.familyName.text.toString()
        val newStatus = Friend.Status.intToStatus(_binding.status.selectedItemId.toInt())
        if (newStatus != friend!!.status && 1L != friend!!.id) {
            friend!!.status = newStatus
            thread {
                val command =
                    CommandFactory.makeAskingAccess(friend!!, contacts!!.get_contact(1L)!!)
                var connection = Connection(friend!!.ip)
                connection.openConnection()
                connection.sendData(command.toString())
            }
        }
        friend!!.ip = _binding.IP.text.toString()
        Log.d(TAG,"Save friend: ${friend!!.getJson()}")
        friend!!.id?. takeIf {it > 0}?. let { contacts!!.updateContact(friend!!) } ?:
        contacts!!.add_friend(friend!!)
        this.findNavController().popBackStack(R.id.navigation_contact_own_data_editing, true)
    }
}