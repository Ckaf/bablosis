package com.example

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.LongIdTable
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

object Channels : LongIdTable() {
    val name = text("name")
}

object Users : LongIdTable() {
    val name = text("name")
    val password = text("password")
    val email = text("email")
    val telegram = text("telegram").nullable()
    val confirmed = bool("confirmed").default(false)
    val isAdmin = bool("is_admin").default(false)
    val isIshtar = bool("is_ishtar").default(false)
    val isCourier = bool("is_courier").default(false)
}

object ChannelsBots : LongIdTable() {
    val channelId = reference("channel_id", Channels)
    val userId = reference("user_id", Users)
}

object Posts : LongIdTable() {
    val postTgId = long("post_tg_id")
    val channelsBotsId = reference("id_channelsbots", ChannelsBots)
}

suspend fun addUser(user: User) {
    DatabaseFactory.dbQuery {
        Users.insert {
            it[name] = user.name
            it[password] = user.password
            it[email] = user.email
            it[telegram] = user.telegram
            it[confirmed] = user.confirmed
            it[isAdmin] = user.isAdmin
            it[isIshtar] = user.isIshtar
            it[isCourier] = user.isCourier
        }
    }
}


suspend fun userExists(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        Users.select { Users.email eq email }
            .count() > 0
    }
}

suspend fun isValidUser(email: String, password: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.let {
            it[Users.password] == password
        } ?: false
    }
}

suspend fun isUserConfirmed(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.let {
            it[Users.confirmed]
        } ?: false
    }
}

suspend fun isUserCourier(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.let {
            it[Users.isCourier]
        } ?: false
    }
}

suspend fun isUserAdmin(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.let {
            it[Users.isAdmin]
        } ?: false
    }
}

suspend fun isUserIshtar(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.let {
            it[Users.isIshtar]
        } ?: false
    }
}

suspend fun confirmUser(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[Users.confirmed] = true
        }
        updatedRows > 0
    }
}

suspend fun setAdmin(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[Users.isAdmin] = true
        }
        updatedRows > 0
    }
}
suspend fun setIshtar(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[Users.isIshtar] = true
        }
        updatedRows > 0
    }
}

suspend fun setCourier(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[Users.isCourier] = true
        }
        updatedRows > 0
    }
}

suspend fun setTelegram(email: String, tgToken:String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[Users.telegram] = tgToken
        }
        updatedRows > 0
    }
}

suspend fun getTelegram(email: String): String? {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.get(Users.telegram)
    }
}

suspend fun getAdminTelegram(): String? {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.isAdmin eq true }.single()
        user[Users.telegram]
    }
}

suspend fun getAllUsers(): List<User> {
    return DatabaseFactory.dbQuery {
        Users.selectAll().map {
            User(
                id = it[Users.id].value,
                name = it[Users.name],
                password = it[Users.password],
                email = it[Users.email],
                telegram = it[Users.telegram],
                confirmed = it[Users.confirmed],
                isAdmin = it[Users.isAdmin],
                isIshtar = it[Users.isIshtar],
                isCourier = it[Users.isCourier]
            )
        }
    }
    }

suspend fun getAllChannelNames(): List<String> {
    return DatabaseFactory.dbQuery {
        Channels.slice(Channels.name).selectAll().map { it[Channels.name] }
    }
}

suspend fun getBotTokensByChannelName(channelName: String): List<String?> {
    return DatabaseFactory.dbQuery {
        (Channels innerJoin ChannelsBots innerJoin Users)
            .slice(Users.telegram)
            .select { Channels.name eq channelName }
            .map { it[Users.telegram] }
    }
}

suspend fun addPost(email: String, channelName: String, postTgId: Long) {
    DatabaseFactory.dbQuery {
        val userId = Users
            .select { Users.email eq email }
            .map { it[Users.id].value }
            .singleOrNull()

        val channelId = Channels
            .select { Channels.name eq channelName }
            .map { it[Channels.id].value }
            .singleOrNull()

        if (userId != null && channelId != null) {
            val channelsBotsId = ChannelsBots
                .select { ChannelsBots.userId eq userId and (ChannelsBots.channelId eq channelId) }
                .map { it[ChannelsBots.id].value }
                .singleOrNull()

            if (channelsBotsId != null) {
                Posts.insert {
                    it[Posts.postTgId] = postTgId
                    it[Posts.channelsBotsId] = channelsBotsId
                }
            } else {
                println("ChannelsBots entry not found for userId: $userId and channelId: $channelId")
            }
        } else {
            if (userId == null) println("User with email: $email not found")
            if (channelId == null) println("Channel with name: $channelName not found")
            null
        }
    }
}


