package com.example.whatsappclone.api

import com.example.whatsappclone.utils.Constants.FCM_BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object{
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(FCM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val apiInstance by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}