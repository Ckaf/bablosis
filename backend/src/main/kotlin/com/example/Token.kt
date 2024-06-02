package com.example

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

typealias Token = String;

class TokenFactory(){

    enum class roles_enum(str: String){
        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN"),
        ISHTAR("ROLE_ISHTAR"),
        NONE("ROLE_NONE")
    }
  //  private val roles = listOf("ROLE_USER", "ROLE_ADMIN, ROLE_ISHTAR")
    private val secretKey:String = "babloisis_secret"
    private val validityAccess: Long = 3600000 // 1 час
    private val validityRefresh: Long = 259200000 // 3 дня

   // val mutableMap: MutableMap<Token, String> = mutableMapOf()

    fun genToken(username:String): Token {
        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = roles_enum.entries.toTypedArray()

        val now = Date()
        val validity = Date(now.time + validityAccess)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun getUsernameFromToken(token: Token): String {
            val claims: Claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
            return  claims.subject
    }

    private fun getRolesFromToken(token: Token): roles_enum? {
        return try {
            val claims: Claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
            claims["roles"] as? roles_enum
        } catch (e: Exception) {
            null
        }
    }

    fun genRefreshToken(accessToken: Token): Token {

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

    suspend fun hasAccess(accessToken: Token, requiredRole:roles_enum):Boolean{
        return when(getRolesFromToken(accessToken)){
            roles_enum.ADMIN -> true
            roles_enum.ISHTAR -> {
                requiredRole == roles_enum.ISHTAR
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
