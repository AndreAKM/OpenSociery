package com.example.opensociety.ui.contacts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val root: View = binding.root

        //val textView: TextView = binding.ownContactInfo
        var contactsList: RecyclerView = binding.contactsList
        contactsList.layoutManager = LinearLayoutManager(context)
        contactsList.adapter = activitynContext?.let { ContactsListAdapter(it) }
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