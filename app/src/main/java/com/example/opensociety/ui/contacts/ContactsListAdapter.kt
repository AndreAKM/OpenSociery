package com.example.opensociety.ui.contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.opensociety.R
import com.example.opensociety.db.Contacts

class ContactsListAdapter(context:Context): RecyclerView.Adapter<ContactsListAdapter.ViewHolder>() {
    val context = context
    var contacts = Contacts(context).contactsList()

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
        holder.largeTextView?.text = when {
            contacts[position].nick.isNotEmpty() -> contacts[position].nick
            contacts[position].family_name.isNotEmpty() -> contacts[position].family_name
            contacts[position].family_name.isNotEmpty() -> contacts[position].family_name
            else -> ""
        }
        holder.smallTextView?.text = contacts[position].status.toString() +
                "\n" + contacts[position].ip
    }

    override fun getItemCount(): Int {
        return contacts.size
    }


}