package com.example.whatsappclone.model


data class User(
    val name:String,
    val imageUrl:String,
    val thumbnail:String,
    val status:String,
    val onlineStatus:Int=0,
    val deviceToken:String,
    val uid:String,
    val phoneNumber:Long=1234567890
){
    //We need Empty Constructor to Work with Firebase
    constructor():this("","","","",0,"","",1234567890)
    constructor(name: String,imageUrl: String,thumbnail: String,uid: String,phoneNumber:Long):this(name,imageUrl,thumbnail,"" +
            "Hey! There I am using ChitChat.",0,"",uid,phoneNumber)
    constructor(name: String,imageUrl: String,thumbnail: String,uid: String,status: String,phoneNumber: Long):this(name,imageUrl,thumbnail,status,0,"",uid,phoneNumber)
}