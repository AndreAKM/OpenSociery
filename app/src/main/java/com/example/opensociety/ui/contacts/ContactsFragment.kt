package com.example.opensociety.ui.contacts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opensociety.R
import com.example.opensociety.databinding.FragmentContacsListBinding

class ContactsFragment : Fragment() {

    private lateinit var contactsViewModel: ContactsViewModel
    private var _binding: FragmentContacsListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    var activitynContext : FragmentActivity? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activitynContext = context as FragmentActivity
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        contactsViewModel =
                ViewModelProvider(this).get(ContactsViewModel::class.java)

        _binding = FragmentContacsListBinding.inflate(inflater, container, false)
        val root: View = _binding!!.root
        _binding!!.addContact.setOnClickListener {
            findNavController().navigate(
                R.id.action_navigation_contacts_list_to_navigation_contact_data) }

        //val textView: TextView = binding.ownContactInfo
        var contactsList: RecyclerView = binding.contactsList
        contactsList.layoutManager = LinearLayoutManager(context)
        var adapter = activitynContext?.let { ContactsListAdapter(it) }
        contactsList.adapter = adapter
        if(adapter?.itemCount == 0) {
            val bundle = context?. let{bundleOf(it.getString(R.string.contact_id) to 1L)}
            findNavController().navigate(
                R.id.action_navigation_contacts_list_to_navigation_contact_data, bundle)
        }
        /*contactsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}