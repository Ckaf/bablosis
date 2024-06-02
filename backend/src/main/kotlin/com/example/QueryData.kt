package com.example

import kotlinx.serialization.Serializable

@Serializable
data class LoginData(val username: String, val password: String)

@Serializable
data class  TgBindData(val accessToken: Token, val tg: String)

@Serializable
data class  RoleSetData(val accessToken: Token, val username: String, val role: String)