package com.example.whatsappclone

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.example.whatsappclone.databinding.ChatListItemBinding
import com.example.whatsappclone.model.Inbox
import com.example.whatsappclone.utils.formatAsListItem


class ChatsViewHolder(val binding:ChatListItemBinding):RecyclerView.ViewHolder(binding.root) {

    fun bind(inbox: Inbox, onClick: (name:String, uid:String, url:String) -> Unit){
        binding.tvUsername.text=inbox.name
        binding.tvStatus.text=inbox.message
        Glide.with(binding.root.context).load(inbox.image).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(binding.ivProfile)
        if(inbox.count>0){
            binding.tvCount.visibility= View.VISIBLE
            binding.tvCount.text=inbox.count.toString()
        }else{
            binding.tvCount.visibility=View.INVISIBLE
        }
//        binding.root.setOnClickListener {
//            onClick.invoke(inbox.name,inbox.fromId,inbox.image)
//        }
        binding.tvTime.text=inbox.time.formatAsListItem(binding.root.context)
        itemView.setOnClickListener {
            onClick.invoke(inbox.name,inbox.fromId,inbox.image)
        }
    }

    class WrapContentLinearLayoutManager(val context:Context) : LinearLayoutManager(context) {

        override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("TAG", "meet a IOOBE in RecyclerView")
            }
        }
    }
}