package com.example.whatsappclone.adapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.whatsappclone.NAME
import com.example.whatsappclone.R
import com.example.whatsappclone.URL
import com.example.whatsappclone.databinding.ActivityProfilePictureBinding

class ProfilePicture : AppCompatActivity() {
    private val url by lazy {
        intent.getStringExtra(URL)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }
    private lateinit var binding:ActivityProfilePictureBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfilePictureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(binding.root.context).load(url).placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar).into(binding.ivProfilePic)
        binding.tvProfileName.text=name
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}