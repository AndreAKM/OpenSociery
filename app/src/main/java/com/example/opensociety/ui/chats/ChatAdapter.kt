package com.example.opensociety.ui.chats

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.opensociety.db.Message
import com.example.opensociety.R

import android.widget.TextView

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.ImageView
import com.example.opensociety.db.Contacts
import com.example.opensociety.ui.contacts.ContactsListAdapter

class ChatAdapter(context: Context, chatID: Long): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val context = context
    val chatID = chatID
    val contacts = Contacts(context)
    var messageList = emptyList<Message>()
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    override fun getItemCount(): Int {
        return messageList.size
    }

    // Determines the appropriate ViewType according to the sender of the message.
    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.author_id == 1L) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_own_message_layout, parent, false)
                SentMessageHolder(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_other_message_layout, parent, false)
                ReceivedMessageHolder(view)
            }
        }
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message)
        }
    }

    private inner class ReceivedMessageHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var messageText: TextView =
            itemView.findViewById<View>(R.id.text_gchat_user_other) as TextView
        var timeText: TextView =
            itemView.findViewById<View>(R.id.text_gchat_timestamp_other) as TextView
        var nameText: TextView =
            itemView.findViewById<View>(R.id.text_gchat_message_other) as TextView
        var profileImage: ImageView =
            itemView.findViewById<View>(R.id.image_gchat_profile_other) as ImageView

        fun bind(message: Message) {
            messageText.text = message.body

            // Format the stored timestamp into a readable String using method.
            timeText.text = message.time
            nameText.text = contacts.get_contact(message.author_id)?.getTitle()

            // Insert the profile image from the URL into the ImageView.
            /*Utils.displayRoundImageFromUrl(
                mContext,
                message.getSender().getProfileUrl(),
                profileImage
            )*/
        }

    }
    private class SentMessageHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById(R.id.card_gchat_message_me)
        var timeText: TextView = itemView.findViewById(R.id.text_gchat_timestamp_me)
        fun bind(message: Message) {
            messageText.text = message.body

            // Format the stored timestamp into a readable String using method.
            timeText.text = message.time
        }

    }

}