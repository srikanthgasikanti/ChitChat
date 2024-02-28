package com.example.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class SplashActivity : AppCompatActivity() {
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUid by lazy {
        FirebaseAuth.getInstance().uid
    }
    private val documentReference by lazy {
        fireStore.collection("users").document(currentUid.toString())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            documentReference.update(DEVICE_TOKEN,token)
        })
        val splashDelay=2000L
        window.statusBarColor=(ContextCompat.getColor(this,R.color.white))
        Handler(Looper.getMainLooper()).postDelayed({
            if(auth.currentUser==null){
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
        },splashDelay)
    }
}