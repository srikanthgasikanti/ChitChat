package com.example.whatsappclone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

const val ONLINE_STATUS="onlineStatus"
open class BaseActivity:AppCompatActivity() {
    private lateinit var documentReference:DocumentReference
    private val currentUid by lazy {
        FirebaseAuth.getInstance().uid
    }
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentReference=fireStore.collection("users").document(currentUid.toString())
    }


    override fun onResume() {
        super.onResume()
        documentReference.update(ONLINE_STATUS,1)
    }


    override fun onPause() {
        super.onPause()
        documentReference.update(ONLINE_STATUS,0)
    }
}