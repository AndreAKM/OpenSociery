package com.example.opensociety.ui.contacts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.opensociety.R
import com.example.opensociety.db.Contacts

class ContactsListAdapter(context:FragmentActivity): RecyclerView.Adapter<ContactsListAdapter.ViewHolder>() {
    val context = context
    var contacts = Contacts(context).contactsAlphavitList()
    val TAG = "ContactsListAdapter"
    val CONTACT_DATA = context.getString(R.string.contact_data)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var largeTextView: TextView? = null
        var smallTextView: TextView? = null

        init {
            largeTextView = itemView.findViewById(R.id.name)
            smallTextView = itemView.findViewById(R.id.data)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_recycle_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.largeTextView?.text = contacts[position].getTitle()

        holder.smallTextView?.text = contacts[position].status.toString() +
                "\n" + contacts[position].ip
        holder.itemView.setOnClickListener{
            Log.d(TAG, "click Item: " + position)
            val bundle = bundleOf(CONTACT_DATA to
                contacts[position].getWholeJson().toString())

            Log.d(TAG, "bundle: $bundle")
            holder.itemView.findNavController().navigate(
                R.id.action_navigation_contacts_list_to_navigation_contact_data, bundle)
        }
    }

    override fun getItemCount(): Int {
    return contacts.size
    }
}