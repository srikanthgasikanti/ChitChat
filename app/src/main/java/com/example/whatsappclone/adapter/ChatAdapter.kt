package com.example.whatsappclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.model.ChatEvent
import com.example.whatsappclone.model.ChatHeader
import com.example.whatsappclone.model.Message
import com.example.whatsappclone.utils.formatAsTime

class ChatAdapter(private val list:MutableList<ChatEvent>, private val currentId:String):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate={layout:Int->
              LayoutInflater.from(parent.context).inflate(layout,parent,false)
        }
         return when(viewType){
            TEXT_MESSAGE_RECEIVED->{ MessageViewHolder(inflate(R.layout.list_item_message_recieve)) }
            TEXT_MESSAGE_SENT->{ MessageViewHolder(inflate(R.layout.list_item_sent_message)) }
             DATE_HEADER->{DateViewHolder(inflate(R.layout.list_item_date_header))}
            else->{MessageViewHolder(inflate(R.layout.list_item_message_recieve))}
        }
    }

    override fun onBindViewHolder(holder:RecyclerView.ViewHolder, position: Int) {
        when(val item=list[position]){
            is ChatHeader->{
                val tvDateHeader=holder.itemView.findViewById<TextView>(R.id.dateHeader)
                tvDateHeader.text=item.date
            }
            is Message->{
                val tvMessage=holder.itemView.findViewById<TextView>(R.id.tvMessage)
                val tvTime=holder.itemView.findViewById<TextView>(R.id.tvTime)
                val tvMarkAsRead=holder.itemView.findViewById<ImageView>(R.id.tvMarkAsRead)
                tvTime.text=item.sentAt.formatAsTime()
                tvMessage.text=item.msg
                if(item.senderId==currentId){
                    if(item.readStatus==0)
                        tvMarkAsRead.setImageResource(R.drawable.pink_image)
                    else{
                        tvMarkAsRead.setImageResource(R.drawable.image_message_sent)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(val event=list[position]){
            is Message->{
                if(event.senderId==currentId){
                    TEXT_MESSAGE_SENT
                }else{
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is ChatHeader->{ DATE_HEADER }
            else->{ UNSUPPORTED}
        }
    }

    class MessageViewHolder(view: View):RecyclerView.ViewHolder(view)
    class DateViewHolder(view: View):RecyclerView.ViewHolder(view)

    companion object{
        const val UNSUPPORTED=-1
        const val TEXT_MESSAGE_SENT=0
        const val TEXT_MESSAGE_RECEIVED=1
        const val DATE_HEADER=2
    }
}