package com.example

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import okhttp3.Address


@Serializable
data class RegData(val username: String, val password: String, val email:String)

@Serializable
data class LoginData(val password: String, val email:String)

@Serializable
data class  TgBindData(val accessToken: Token, val tg: String)

@Serializable
data class  RoleSetData(val accessToken: Token, val email: String, val role: String)

@Serializable
data class AccessTokenData(val accessToken: Token)

@Serializable
data class PostData(val accessToken: Token, val channelId:String, val msg: String,  val date: LocalDateTime)

@Serializable
data class OrderCreateData(val accessToken: Token, val bablos: Int, val address: String)

@Serializable
data class OrderData(val accessToken: Token, val orderId:Long, val status: status_enum?)

@Serializable
data class BablosData(val accessToken: Token, val bablos: Int)

@Serializable
data class Chat(val id: Long)

@Serializable
data class Message(val message_id: Long, val from: User, val chat: Chat, val date: Int, val text: String?)

@Serializable
data class GetMessageResponse(val ok: Boolean, val result: Message)

@Serializable
data class GetChatResponse(val ok: Boolean, val result: Chat)