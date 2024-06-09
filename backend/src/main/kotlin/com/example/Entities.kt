package com.example

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id : Long,
    val name : String,
    val password: String,
    val email: String,
    val telegram  : String?,
    val confirmed: Boolean,
    val isAdmin: Boolean,
    val isIshtar: Boolean,
    val isCourier:Boolean
) {
    constructor(_name:String, _password:String, _email:String) :
            this(0, _name, _password, _email,null, false, false, false, false)
}

data class Channel(val id: Long, val name: String)
data class ChannelBot(val id: Long, val channelId: Long, val userId: Long)
data class Post(val id: Long, val postTgId: Long, val channelsBotsId: Long)