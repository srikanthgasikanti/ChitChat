package com.example.whatsappclone

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.databinding.PeopleListItemBinding
import com.example.whatsappclone.model.User

class UserViewHolder(private val binding:PeopleListItemBinding):RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User, onClick:(name:String, uid:String, imageUrl:String)->Unit){
        binding.tvContactName.text=user.name
        binding.tvContactStatus.text=user.status
        Glide.with(binding.root.context).load(user.imageUrl).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(binding.ivContactProfile)
        itemView.setOnClickListener {
            onClick.invoke(user.name,user.uid,user.imageUrl)
        }
    }
}