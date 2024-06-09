package com.example

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.util.*

typealias Token = String;

class TokenFactory(){

    enum class roles_enum(val role: String){
        USER("USER"),
        ADMIN("ADMIN"),
        ISHTAR("ISHTAR"),
        COURIER("COURIER"),
        NONE("NONE");

        companion object {
            fun fromValue(value: String): roles_enum? {
                return entries.find { it.role == value }
            }
        }
    }
  //  private val roles = listOf("ROLE_USER", "ROLE_ADMIN, ROLE_ISHTAR")
    private val secretKey:String = "babloisissecretdjfnv1231234124123dknfkvdjfvnskjdfnvkjsdnfv"
    private val validityAccess: Long = 3600000 // 1 час
    private val validityRefresh: Long = 259200000 // 3 дня

   // val mutableMap: MutableMap<Token, String> = mutableMapOf()
     private fun getSigningKey() : Key {
        val keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private suspend fun getUserRole(email:String):roles_enum{
        if(isUserAdmin(email)) return roles_enum.ADMIN
        if (isUserIshtar(email)) return roles_enum.ISHTAR
        if (isUserCourier(email)) return roles_enum.COURIER
        if (isUserConfirmed(email)) return roles_enum.USER
        return roles_enum.NONE
    }

    suspend fun genToken(email:String): Token {
        val claims = Jwts.claims().setSubject(email)
        val role = getUserRole(email)
        claims["roles"] = role

        val now = Date()
        val validity = Date(now.time + validityAccess)
       // val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun getEmailFromToken(token: Token): String {
            val claims: Claims = Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token).body
            return  claims.subject
    }

     private fun getRoleFromToken(token: Token): roles_enum? {
         return try {
             val claims: Claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).body
             val role = claims["roles"] as? String
             roles_enum.fromValue(role ?: "")
         } catch (e: Exception) {
             println(e)
             null
         }
    }

    fun genRefreshToken(accessToken: Token): Token { //todo переписать как genToken

            val claims: Claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken).body
            val now = Date()
            val validity = Date(now.time + validityRefresh)

            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact()

    }

    fun hasAccess(accessToken: Token, requiredRole:roles_enum):Boolean{
        return when(getRoleFromToken(accessToken)){
            roles_enum.ADMIN -> true
            roles_enum.ISHTAR -> {
                requiredRole == roles_enum.ISHTAR
            }
            roles_enum.COURIER ->{
                requiredRole == roles_enum.COURIER
            }
            roles_enum.USER -> {
                requiredRole == roles_enum.USER
            }
            roles_enum.NONE -> {
                //todo generate_new_token
                false
            }

            null -> false
        }
        }
    }
