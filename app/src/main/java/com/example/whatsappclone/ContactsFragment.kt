package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.databinding.EmptyListItemBinding
import com.example.whatsappclone.databinding.FragmentContactsBinding
import com.example.whatsappclone.databinding.PeopleListItemBinding
import com.example.whatsappclone.model.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

const val NORMAL_VIEW_TYPE=1
const val DELETED_VIEW_TYPE=2
const val NAME="name"
const val UID="uid"
const val URL="url"
const val EDIT="edit"
const val STATUS="status"
class ContactsFragment : Fragment(R.layout.fragment_contacts) {
    private lateinit var binding:FragmentContactsBinding

    private lateinit var adapter:FirestorePagingAdapter<User,RecyclerView.ViewHolder>
    private  val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance().collection("users").orderBy("name",Query.Direction.ASCENDING)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentContactsBinding.bind(view)
        setAdapter()
        binding.rvPeople.adapter=adapter
        binding.progressBar.visibility=View.GONE
    }

    private fun setAdapter() {
        val config= PagingConfig(10,2,false)
        val pagingOptions= FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(database,config, User::class.java)
            .build()
        adapter=object :FirestorePagingAdapter<User, RecyclerView.ViewHolder>(pagingOptions){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return when(viewType){
                    1->{
                        val view=layoutInflater.inflate(R.layout.people_list_item,parent,false)
                        UserViewHolder(PeopleListItemBinding.bind(view))
                    }
                    else->{
                        val view=layoutInflater.inflate(R.layout.empty_list_item,parent,false)
                        EmptyViewHolder(EmptyListItemBinding.bind(view))
                    }
                }
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if(holder is UserViewHolder){
                    holder.bind(model){ name: String, uid: String, url: String ->
                        val intent= Intent(requireContext(),ChatActivity::class.java)
                        intent.putExtra(NAME,name)
                        intent.putExtra(UID,uid)
                        intent.putExtra(URL,url)
                        startActivity(intent)
                    }
                }
            }

            override fun getItemViewType(position: Int): Int {
                val item=getItem(position)!!.toObject(User::class.java)
                return if(item!!.uid==auth.uid){
                    DELETED_VIEW_TYPE
                }else{
                    NORMAL_VIEW_TYPE
                }
            }
        }
    }
}