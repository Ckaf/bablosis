package com.example

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id : Int,
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

