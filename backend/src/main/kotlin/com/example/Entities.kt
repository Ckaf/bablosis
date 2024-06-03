package com.example

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id : Long,
    val name : String,
    val password: String,
    val telegram  : String?,
    val confirmed: Boolean,
    val is_admin: Boolean,
    val is_ishtar: Boolean
) {
    constructor(_name:String, _password:String) :
            this(0, _name, _password, null, false, false, false)
}

data class Channel(val id: Long, val name: String)
data class ChannelBot(val id: Long, val channelId: Long, val userId: Long)