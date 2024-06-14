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


@Serializable
data class Order(
    val id : Long,
    val idUser : Long,
    val idCourier: Long?,
    val address: String,
    val bablos  : Int,
    val status: status_enum,
) {
    constructor(_id:Long, _idUser:Long, _address:String, _bablos:Int) :
            this(_id, _idUser, null, _address,_bablos, status_enum.NONE)
}

enum class status_enum(val status: String){
    NONE("NONE"),
    ASSEMBLY("ASSEMBLY"),
    DELIVERY("DELIVERY"),
    DONE("DONE");

    companion object {
        fun fromValue(value: String): status_enum? {
            return entries.find { it.status == value }
        }
    }
}