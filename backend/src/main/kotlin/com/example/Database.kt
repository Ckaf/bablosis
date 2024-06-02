package com.example

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        val database =
            Database.connect(
                url = "jdbc:postgresql://localhost:5432/bablosis_db",
                driver = "org.postgresql.Driver",
                user = "ckaf",
                password = "",
            )
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}


object Users : Table() {
    val id = long("id").autoIncrement("users_id_seq").uniqueIndex()
    val name = text("name")
    val password = text("password")
    val telegram = text("telegram").nullable()
    val confirmed = bool("confirmed").default(false)
    val isAdmin = bool("is_admin").default(false)
    val isIshtar = bool("is_ishtar").default(false)
}

suspend fun addUser(user: User) {
    DatabaseFactory.dbQuery {
        Users.insert {
            it[name] = user.name
            it[password] = user.password
            it[telegram] = user.telegram
            it[confirmed] = user.confirmed
            it[isAdmin] = user.is_admin
            it[isIshtar] = user.is_ishtar
        }
    }
}


suspend fun userExists(userName: String): Boolean {
    return DatabaseFactory.dbQuery {
        Users.select { Users.name eq userName }
            .count() > 0
    }
}

suspend fun isValidUser(userName: String, password: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.name eq userName }.singleOrNull()
        user?.let {
            it[Users.password] == password
        } ?: false
    }
}

suspend fun isUserConfirmed(userName: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.name eq userName }.singleOrNull()
        user?.let {
            it[Users.confirmed]
        } ?: false
    }
}

suspend fun isUserAdmin(userName: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.name eq userName }.singleOrNull()
        user?.let {
            it[Users.isAdmin]
        } ?: false
    }
}

suspend fun isUserIshtar(userName: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.name eq userName }.singleOrNull()
        user?.let {
            it[Users.isIshtar]
        } ?: false
    }
}

suspend fun confirmUser(userName: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.name eq userName }) {
            it[Users.confirmed] = true
        }
        updatedRows > 0
    }
}

suspend fun setAdmin(userName: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.name eq userName }) {
            it[Users.isAdmin] = true
        }
        updatedRows > 0
    }
}
suspend fun setIshtar(userName: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.name eq userName }) {
            it[Users.isIshtar] = true
        }
        updatedRows > 0
    }
}

suspend fun setTelegram(userName: String, tgToken:String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.name eq userName }) {
            it[Users.telegram] = tgToken
        }
        updatedRows > 0
    }
}