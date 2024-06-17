package com.example

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        val database =
            Database.connect(
                url = "jdbc:postgresql://80.78.242.22:5432/bablosis",
               //url = "jdbc:postgresql://localhost/bablosis_db",
                driver = "org.h2.Driver",
                user = "postgres",
                password = "postgres",
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

object Balance : Table(){
    val bablos = double("bablosis")
    val user_id = reference("user_id", Users)
}

suspend fun initBalance(userId: Long) {
    DatabaseFactory.dbQuery {
        Balance.insert {
            it[user_id] = EntityID(userId, Users)
            it[bablos] = 0.0
        }
    }
}

suspend fun getBalance(userId: Long): Double? {
    return DatabaseFactory.dbQuery {
        val user = Balance.select { Balance.user_id eq userId }.singleOrNull()
        user?.get(Balance.bablos)
    }
}

suspend fun changeBalanceToUser(userId: Long, amount: Double) :Boolean{
    if (amount < 0 && (getBalance(userId)!! < -amount))return false

    DatabaseFactory.dbQuery {
        Balance.update({ Balance.user_id eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(bablos, bablos + amount)
            }
        }
    }
    return true
}


object Orders : LongIdTable() {
    val id_user = reference("id_user", Users)
    val id_courier = reference("id_courier", Users).nullable()
    val address = text("address")
    val bablos = double("bablos")
    val status = text("status")
}

suspend fun addOrder(userId: Long, courierId: Long?, address: String, bablos: Double, status: String) {
    DatabaseFactory.dbQuery {
        Orders.insert {
            it[id_user] = userId
            it[id_courier] = courierId
            it[Orders.address] = address
            it[Orders.bablos] = bablos
            it[Orders.status] = status
        }
    }
}

suspend fun getOrdersByCourierId(courierId: Long): List<Order> {
    return DatabaseFactory.dbQuery {
        Orders.select {
            Orders.id_courier eq courierId
        }.map { row ->
            Order(
                id = row[Orders.id].value,
                idUser = row[Orders.id_user].value,
                idCourier = row[Orders.id_courier]?.value,
                address = row[Orders.address],
                bablos = row[Orders.bablos],
                status = status_enum.valueOf(row[Orders.status])
            )
        }
    }
}



suspend fun getOrdersByUserId(userId: Long): List<Order> {
    return DatabaseFactory.dbQuery {
        Orders.select {
            Orders.id_user eq userId
        }.map { row ->
            Order(
                id = row[Orders.id].value,
                idUser = row[Orders.id_user].value,
                idCourier = row[Orders.id_courier]?.value,
                address = row[Orders.address],
                bablos = row[Orders.bablos],
                status = status_enum.valueOf(row[Orders.status])
            )
        }
    }
}


suspend fun getFreeOrders(): List<Order> {
    return DatabaseFactory.dbQuery {
        Orders.select {
            Orders.id_courier.isNull()
        }.map { row ->
            Order(
                id = row[Orders.id].value,
                idUser = row[Orders.id_user].value,
                idCourier = null,
                address = row[Orders.address],
                bablos = row[Orders.bablos],
                status = status_enum.valueOf(row[Orders.status])
            )
        }
    }
}

suspend fun updateOrderCourier(orderId:Long, courierId:Long ){
    DatabaseFactory.dbQuery {
        Orders.update({ Orders.id eq orderId }) {
            it[id_courier] = courierId
        }
    }
}

suspend fun getOrderStatus(orderId: Long): status_enum? {
    return DatabaseFactory.dbQuery {
        Orders.select { Orders.id eq orderId }
            .mapNotNull { row ->
                status_enum.fromValue(row[Orders.status])
            }
            .singleOrNull()
    }
}

suspend fun updateOrderStatus(orderId: Long, newStatus: status_enum) {
    DatabaseFactory.dbQuery {
        Orders.update({ Orders.id eq orderId }) {
            it[status] = newStatus.status
        }
    }
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
            it[confirmed] = true
        }
        updatedRows > 0
    }
}

suspend fun setAdmin(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[isAdmin] = true
        }
        updatedRows > 0
    }
}
suspend fun setIshtar(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[isIshtar] = true
        }
        updatedRows > 0
    }
}

suspend fun setCourier(email: String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[isCourier] = true
        }
        updatedRows > 0
    }
}

suspend fun setTelegram(email: String, tgToken:String): Boolean {
    return DatabaseFactory.dbQuery {
        val updatedRows = Users.update({ Users.email eq email }) {
            it[telegram] = tgToken
        }
        updatedRows > 0
    }
}

suspend fun getId(email: String): EntityID<Long>? {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.get(Users.id)
    }
}

suspend fun getTelegram(email: String): String? {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.get(Users.telegram)
    }
}

suspend fun getName(email: String): String? {
    return DatabaseFactory.dbQuery {
        val user = Users.select { Users.email eq email }.singleOrNull()
        user?.get(Users.name)
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


