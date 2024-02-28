package com.example.whatsappclone.model

import android.content.Context
import com.example.whatsappclone.utils.formatAsHeader
import java.util.Date


interface ChatEvent{
    val sentAt:Date
}
data class Message(
    val msg:String,
    val senderId:String,
    val msgId:String,
    val type:String="TEXT",
    val liked:Boolean=false,
    var readStatus:Int=1,
    override val sentAt: Date=Date(),
):ChatEvent{
    constructor():this("","","","",false,1, Date())
}

data class ChatHeader(
    override val sentAt:Date,val context:Context
):ChatEvent{
    val date:String=sentAt.formatAsHeader(context)
}