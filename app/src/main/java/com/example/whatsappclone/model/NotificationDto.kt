package com.example.whatsappclone.model

import java.util.Date

data class NotificationDto(
    val message: String,
    var fromId: String,
    var name: String,
    var image: String,
    val time: Date = Date(),
    var phoneNumber: Long = 0,
) {
    constructor() : this("", "", "","", Date(), 0)
}