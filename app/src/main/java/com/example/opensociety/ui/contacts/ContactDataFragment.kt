package com.example.opensociety.ui.contacts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.opensociety.R
import com.example.opensociety.databinding.FragmentContactDataBinding
import com.example.opensociety.db.Contacts
import com.example.opensociety.db.Friend
import org.json.JSONObject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactDataFragment : Fragment() {
    val TAG = "ContactDataFragment"
    private var CONTACT_DATA: String?= null
    private var CONTACT_ID: String? = null

    private lateinit var _binding: FragmentContactDataBinding
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
        _binding = FragmentContactDataBinding.inflate(inflater, container, false)
        val arg =arguments
        Log.d(TAG, "onCreateView($savedInstanceState) {friend: $arg}")
        id = arg!!.getLong(CONTACT_ID, -1L)
        friend = when {
            id == -1L && arg != null -> {
                arg!!.getString(CONTACT_DATA)?.let { json ->
                    Friend(JSONObject(json))
                } ?: Friend()
            }
            id != -1L -> context?.let { c ->
                Contacts(c).get_contact(id!!).also {
                    Log.d(TAG, "ID: $id, friend: ${it?. let{it.getWholeJson().toString()}}")
                }
            } ?: null
            else -> Friend()
        }
        when {
            (friend != null) -> {
                Log.d(TAG, "Friend(${friend!!.id})'s IP: ${friend!!.ip}")
                _binding.title.text = friend!!.getTitle()
                _binding.firsName.text = friend!!.first_name
                _binding.secondName.text = friend!!.second_name
                _binding.familyName.text = friend!!.family_name
                _binding.birthday.text = friend!!.birthday
                _binding.IP.text = friend!!.ip
                _binding.status.text = (friend!!.status.toString())
                id = friend!!.id
            }
            id == 1L -> contacts?.getIPAddress(). also {
                Log.d (TAG, "ID: 1 IP: $it") } .let{
                _binding.IP.text = it
                _binding.status.text = (Friend.Status.OWNER.toString())
                if(contacts?.get_contact(1L) == null)
                    findNavController().navigate(when(id) {
                        1L -> R.id.navigation_contact_own_data_editing
                        else -> R.id.novigation_contact_other_data_editing_fragment
                    }, arg)
            }
        }
        _binding.firsName.isFocusable = false
        _binding.firsName.isClickable = false
        _binding.status.isEnabled = false
        _binding.status.isClickable = false
        Log.d(TAG, "contact_id: $id")
        _binding.btnEdit.setOnClickListener{
                findNavController().navigate(when(id) {
                    1L -> R.id.navigation_contact_own_data_editing
                    else -> R.id.novigation_contact_other_data_editing_fragment
                    }, arg)}
        return _binding.root
    }
}