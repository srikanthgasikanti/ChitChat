package com.example.whatsappclone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.whatsappclone.adapter.ChatAdapter
import com.example.whatsappclone.adapter.ProfilePicture
import com.example.whatsappclone.api.RetrofitInstance
import com.example.whatsappclone.databinding.ActivityChatBinding
import com.example.whatsappclone.model.ChatEvent
import com.example.whatsappclone.model.ChatHeader
import com.example.whatsappclone.model.Inbox
import com.example.whatsappclone.model.Message
import com.example.whatsappclone.model.NotificationDto
import com.example.whatsappclone.model.PushNotification
import com.example.whatsappclone.model.User
import com.example.whatsappclone.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects
import kotlin.properties.Delegates


class ChatActivity : BaseActivity() {
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private val friendId by lazy { intent.getStringExtra(UID) }
    private val url by lazy { intent.getStringExtra(URL) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val currentUid = auth.currentUser!!.uid
    private val fireStore by lazy { FirebaseFirestore.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance("https://whatsappclone-3501c-default-rtdb.asia-southeast1.firebasedatabase.app/") }
    private lateinit var currentUser: User
    private val messages = mutableListOf<ChatEvent>()
    private val positions = mutableMapOf<String,Int>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var deviceToken:String
    private val documentReference by lazy {
        fireStore.collection("users").document(friendId.toString())
    }
    private lateinit var emojiPopup: EmojiPopup
    private var isUserOnline: Boolean = false
    private lateinit var messagesListener:ChildEventListener
    private var phoneNumber by Delegates.notNull<Long>()

    private lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        setUpAdapter()
        setOnClickListener()
        listenToMessages()
    }

    private fun updateReadCount() {
        getChat(currentUid, friendId!!).child("count").setValue(0)
    }

    override fun onPause() {
        super.onPause()
        getChat(currentUid,friendId!!).child("count").get().addOnSuccessListener { count ->
            val lastChat=count.getValue(Int::class.java)
            lastChat?.let {
                Log.d("Count",lastChat.toString())
                if(it>0){
                    updateReadCount()
                }
            }
        }
        getMessages(friendId!!).removeEventListener(messagesListener)
    }

    private fun initialize() {
        binding.nameTv.text = name
        emojiPopup = EmojiPopup(binding.rootView, binding.msgEdtv)
        Glide.with(binding.root.context).load(url).placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar).into(binding.userImgView)
        fireStore.collection("users").document(currentUid).get().addOnSuccessListener {
            currentUser = it.toObject(User::class.java)!!
        }
        fireStore.collection("users").document(friendId!!).get().addOnSuccessListener {
            val friend = it.toObject(User::class.java)!!
            deviceToken=friend.deviceToken
            phoneNumber=friend.phoneNumber
            Log.d("phone",friend.phoneNumber.toString())
            Log.d("phone",phoneNumber.toString())
        }
        getChat(currentUid,friendId!!).get().addOnSuccessListener {
            val lastChat=it.getValue(Inbox::class.java)
            lastChat?.let {
                if(lastChat.count>0){
                    updateReadCount()
                }
            }
        }
    }

    private fun listenUserOnlineStatus() {
        documentReference.addSnapshotListener(this@ChatActivity, EventListener { value, error ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                if (value.getLong(ONLINE_STATUS) != null) {
                    val isAvailable = Objects.requireNonNull(value.getLong(ONLINE_STATUS)!!).toInt()
                    isUserOnline = isAvailable == 1
                }
            }
            if(isUserOnline){
                binding.tvOnline.visibility= View.VISIBLE
            }else{
                binding.tvOnline.visibility= View.GONE
            }
        })
    }



    private fun setUpAdapter() {
        chatAdapter = ChatAdapter(messages, currentUid)
        binding.msgRv.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun setOnClickListener() {
        binding.sendBtn.setOnClickListener {
            val msg = binding.msgEdtv.text?.trim().toString()
            if (msg.isNotEmpty()) {
                binding.msgEdtv.setText("")
                sendMessage(msg)
            }
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.smileBtn.setOnClickListener {
            if (emojiPopup.isShowing) {
                binding.smileBtn.setImageResource(R.drawable.ic_chat_icons_smile)
                emojiPopup.dismiss()
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.msgEdtv, 0)
            } else {
                binding.smileBtn.setImageResource(R.drawable.baseline_keyboard_24)
                emojiPopup.toggle()
            }
        }
        binding.userImgView.setOnClickListener{
            val intent=Intent(this,ProfilePicture::class.java)
            intent.putExtra(NAME,name)
            intent.putExtra(URL,url)
            startActivity(intent)
        }
        binding.nameTv.setOnClickListener {
            val intent=Intent(this,ProfilePicture::class.java)
            intent.putExtra(NAME,name)
            intent.putExtra(URL,url)
            startActivity(intent)
        }
    }

    private fun sendMessage(msg: String) {
        val msgId = getMessages(friendId!!).push().key
        checkNotNull(msgId) { "Cannot be Null" }
        val msgMap = Message(msg, currentUid, msgId)
        getMessages(friendId!!).child(msgId).setValue(msgMap).addOnSuccessListener {
            updateLastMessage(msgMap)
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
        Log.d("Online",isUserOnline.toString())
        if (!isUserOnline) {
            sendNotification(msgMap)
        }
    }

    private fun sendNotification(msgMap: Message) = CoroutineScope(Dispatchers.IO).launch {
        val inbox = Inbox(msgMap.msg, currentUser.uid, currentUser.name, currentUser.imageUrl,msgMap.sentAt,currentUser.phoneNumber)
        val notification=NotificationDto(msgMap.msg,currentUser.uid,currentUser.name,currentUser.imageUrl,msgMap.sentAt,0)
        try {
            val response = RetrofitInstance.apiInstance.postNotification(PushNotification(inbox,deviceToken))
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Response is Successful")
            } else {
                Log.d(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
        }
    }

    private fun updateLastMessage(msgMap: Message) {
        val inboxMap = Inbox(msgMap.msg, friendId!!, name!!, url!!, msgMap.sentAt, 0)
        getChat(currentUid, friendId!!).setValue(inboxMap).addOnSuccessListener {
            getChat(friendId!!, currentUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val inboxValue = snapshot.getValue(Inbox::class.java)
                    inboxMap.apply {
                        fromId = currentUid
                        name = currentUser.name
                        image = currentUser.imageUrl
                        count = 1
                    }

                    inboxValue?.let {
                        if (it.fromId == currentUid) {
                            inboxMap.count = inboxValue.count + 1
                        }
                    }
                    getChat(friendId!!, currentUid).setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun listenToMessages() {
        messagesListener=getMessages(friendId!!).orderByKey().addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val newMessage = snapshot.getValue(Message::class.java)
                Log.d(TAG,newMessage!!.readStatus.toString())
                addMessage(newMessage)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val newMessage = snapshot.getValue(Message::class.java)
                updateChat(newMessage!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    fun  updateChat(msg:Message){
        val index=positions[msg.msgId]
        messages.removeAt(index!!)
        messages.add(index,msg)
        chatAdapter.notifyItemChanged(index)
    }

    private fun addMessage(newMessage: Message) {
        val eventBefore = messages.lastOrNull()
        if ((eventBefore != null && !eventBefore.sentAt.isSameDayAs(newMessage.sentAt)) || eventBefore == null) {
            messages.add(ChatHeader(newMessage.sentAt, context = this))
        }
        if(newMessage.senderId==friendId){
            val map= mutableMapOf<String,Int>()
            map["readStatus"] = 0
            getMessages(friendId!!).child(newMessage.msgId).updateChildren(map as Map<String, Any>)
        }
        messages.add(newMessage)
        positions[newMessage.msgId]=messages.size-1
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.msgRv.scrollToPosition(messages.size - 1)
    }

    private fun getMessages(fromUser: String) =
        database.reference.child("messages/${getId(fromUser)}")

    private fun getChat(toUser: String, fromUser: String) =
        database.reference.child("chats/$toUser/$fromUser")
    private fun getId(friendId: String): String {
        return if (currentUid > friendId) {
            friendId + currentUid
        } else {
            currentUid + friendId
        }
    }

    override fun onResume() {
        super.onResume()
        listenUserOnlineStatus()
    }
}