package com.example.whatsappclone

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.whatsappclone.databinding.ActivityMainBinding
import com.example.whatsappclone.model.User
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {
    private lateinit var binding:ActivityMainBinding
    private var currentUser:User?=null
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUid by lazy {
        FirebaseAuth.getInstance().uid
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.viewPager.adapter=ScreenSliderAdapter(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                askNotificationPermission()
            }
        }
        TabLayoutMediator(binding.tabs,binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Chats"
                else -> tab.text = "People"
            }
        }.attach()
        fireStore.collection("users").document(currentUid!!).get().addOnSuccessListener {
            currentUser= it.toObject(User::class.java)!!
            Glide.with(binding.root.context).load(currentUser!!.imageUrl).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(binding.userImgView)
        }
        binding.userImgView.setOnClickListener {
            currentUser?.let {
                val intent=Intent(this,SignUpActivity::class.java)
                intent.putExtra(NAME,currentUser!!.name)
                intent.putExtra(UID,currentUser!!.name)
                intent.putExtra(URL,currentUser!!.imageUrl)
                intent.putExtra(EDIT,true)
                intent.putExtra(STATUS,currentUser!!.status)
                intent.putExtra(PHONE_NUMBER,currentUser!!.phoneNumber.toString())
                startActivity(intent)
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            Toast.makeText(this@MainActivity,"We will Send you notifications", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity,"Please Provide Notification Permissions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                //FCM
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}