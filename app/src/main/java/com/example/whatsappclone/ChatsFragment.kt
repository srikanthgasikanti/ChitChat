package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.databinding.ChatListItemBinding
import com.example.whatsappclone.databinding.FragmentChatsBinding
import com.example.whatsappclone.model.Inbox
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ChatsFragment : Fragment(R.layout.fragment_chats) {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var adapter: FirebaseRecyclerAdapter<Inbox, ChatsViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val list = mutableListOf<Inbox>()
    private val database by lazy { FirebaseDatabase.getInstance("https://whatsappclone-3501c-default-rtdb.asia-southeast1.firebasedatabase.app/") }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewManager = LinearLayoutManager(requireContext())
        setAdapter()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatsBinding.bind(view)
        binding.rvChats.apply {
            layoutManager = ChatsViewHolder.WrapContentLinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@ChatsFragment.adapter
        }
    }

    private fun setAdapter() {
        val baseQuery = database.reference.child("chats").child(auth.uid!!)

        val options = FirebaseRecyclerOptions.Builder<Inbox>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(baseQuery, Inbox::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<Inbox, ChatsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_list_item, parent, false)
                return ChatsViewHolder(ChatListItemBinding.bind(view))
            }

            override fun onBindViewHolder(holder: ChatsViewHolder, position: Int, model: Inbox) {
                // val inbox = list[position]
                holder.bind(model) { name, uid, url ->
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                    intent.putExtra(NAME, name)
                    intent.putExtra(UID, uid)
                    intent.putExtra(URL, url)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}

//                holder.binding.apply {
//                    tvUsername.text = inbox.name
//                    tvStatus.text = inbox.message
//                    Glide.with(requireContext()).load(inbox.image)
//                        .placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar)
//                        .into(ivProfile)
//                    if (inbox.count > 0) {
//                        tvCount.visibility = View.VISIBLE
//                        tvCount.text = inbox.count.toString()
//                    }
//                    tvTime.text = inbox.time.formatAsListItem(requireContext())
//                }
//                holder.itemView.setOnClickListener {
//                    onClick.invoke(inbox.name, inbox.fromId, inbox.image)
//                }
//        adapter = InboxAdapter(list, requireContext()) { name, uid, imageUrl ->
//            val intent = Intent(requireContext(), ChatActivity::class.java)
//            intent.putExtra(NAME, name)
//            intent.putExtra(UID, uid)
//            intent.putExtra(URL, imageUrl)
//            startActivity(intent)
//        }
//        val uid = Firebase.auth.uid.toString()
//        Log.d("UID", auth.uid.toString())
////        val baseQuery= Firebase.database("https://whatsappclone-3501c-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("chats").child("QxQceYxFkUcAHPAyKR9u7JWJV873")
//        database.reference.child("chats").child(auth.uid!!).orderByKey()
//            .addChildEventListener(object : ChildEventListener {
//                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                    val value = snapshot.value
//                    Log.d("Chat", value.toString())
//                    val inboxItem = snapshot.getValue(Inbox::class.java)
//                    list.add(inboxItem!!)
//                    adapter.notifyItemInserted(list.size - 1)
//                }
//
//                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//
//                }
//
//                override fun onChildRemoved(snapshot: DataSnapshot) {
//
//                }
//
//                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })


//    class InboxAdapter(val list: MutableList<Inbox>, val context: Context,val onClick:(name:String, uid:String, imageUrl:String)->Unit):RecyclerView.Adapter<ChatsViewHolder>(){
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
//            val view=LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item,parent,false)
//            return ChatsViewHolder(ChatListItemBinding.bind(view))
//        }
//
//        override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
//            val inbox=list[position]
//            holder.binding.apply {
//                tvUsername.text=inbox.name
//                tvStatus.text=inbox.message
//                Glide.with(context).load(inbox.image).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(ivProfile)
//                if(inbox.count>0){
//                    tvCount.visibility= View.VISIBLE
//                    tvCount.text=inbox.count.toString()
//                }
//                tvTime.text=inbox.time.formatAsListItem(context)
//            }
//            holder.itemView.setOnClickListener {
//                onClick.invoke(inbox.name,inbox.fromId,inbox.image)
//            }
//        }
//
//        override fun getItemCount(): Int {
//            return list.size
//        }
//    }

//    override fun onStart() {
//        super.onStart()
//        adapter.startListening()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        adapter.stopListening()
//    }