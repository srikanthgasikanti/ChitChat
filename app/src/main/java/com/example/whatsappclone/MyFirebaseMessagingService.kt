package com.example.whatsappclone

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import java.io.IOException
import java.net.URL


const val CHANNEL_ID = "myChitChatChannel"
const val DEVICE_TOKEN = "deviceToken"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUid by lazy {
        FirebaseAuth.getInstance().uid
    }
    private val documentReference by lazy {
        fireStore.collection("users").document(currentUid.toString())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, token)
        documentReference.update(DEVICE_TOKEN, token)
    }

    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(title: String, message: String, imageUrl: String): RemoteViews {
        val remoteView = RemoteViews("com.example.whatsappclone", R.layout.notification_layout)
        remoteView.setTextViewText(R.id.tvNotificationTitle, title)
        remoteView.setTextViewText(R.id.tvNotificationMessage, message)
        //remoteView.setImageViewResource(R.id.ivNotificationImage,R.drawable.app_logo)
        try {
            val url = URL(imageUrl)
            val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            remoteView.setImageViewBitmap(R.id.ivNotificationImage, image)
        } catch (e: IOException) {
            println(e)
        }
        return remoteView
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val name = message.data["name"]
        val msg = message.data["message"]
        val uid = message.data["fromId"]
        val url = message.data["image"]
        val phoneNumber=message.data["count"]
        Log.d("remote",Gson().toJson(message).toString())
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = (phoneNumber!!.toLong()/10000).toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val imageUrl = URL(url)
        val image = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())
        // Create an Intent for the activity you want to start.
        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra(NAME, name)
        intent.putExtra(UID, uid)
        intent.putExtra(URL, url)
        // Create the TaskStackBuilder.
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack.
            addNextIntentWithParentStack(intent)
            // Get the PendingIntent containing the entire back stack.
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        if(findActiveNotification(this,notificationID)!=null){
            val oldMessage=restoreMessagingStyle(this,notificationID)
            val allMessages=StringBuilder()
            oldMessage?.messages?.forEach {
                allMessages.append("${it.text}\n")
            }
            allMessages.append(msg)
            Log.d("Messages",allMessages.toString())
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setStyle(NotificationCompat.MessagingStyle(androidx.core.app.Person.Builder().setKey(currentUid).setName("You").build()).addMessage(allMessages,System.currentTimeMillis(),androidx.core.app.Person.Builder().setKey(uid).setName(name).setIcon(IconCompat.createWithBitmap(image)).build()))
                .build()
            notificationManager.notify(notificationID,notification)
        }else{
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setStyle(NotificationCompat.MessagingStyle(androidx.core.app.Person.Builder().setKey(currentUid).setName("You").build()).addMessage(msg,System.currentTimeMillis(),androidx.core.app.Person.Builder().setKey(uid).setName(name).setIcon(
                    IconCompat.createWithBitmap(image)).build()))
                .build()
            notificationManager.notify(notificationID, notification)
        }

    }

    private fun findActiveNotification(context: Context, notificationId: Int): Notification? {
        return (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .activeNotifications.find { it.id == notificationId }?.notification
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "GeoMedia"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "Channel Description"
            lightColor = android.graphics.Color.GREEN
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification Channel Created")
    }

    fun restoreMessagingStyle(context: Context, notificationId: Int): NotificationCompat.MessagingStyle? {
        return (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .activeNotifications
            .find { it.id == notificationId }
            ?.notification
            ?.let { NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(it) }
    }
}