package com.example

// import io.ktor.features.*
import com.example.plugins.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf.Constructor

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    DatabaseFactory.init()
    configureDatabases()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting()
}

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

@Serializable data class LoginData(val username: String, val password: String)

fun Application.configureSerialization() {
    install(ContentNegotiation) { json() }
}

fun Application.configureRouting() {
    val logger = LoggerFactory.getLogger("Application")

    routing {
        post("/login") {
            logger.info("GET SOME RESPONSE")
            val loginData = call.receive<LoginData>()
            val username = loginData.username
            val password = loginData.password

            // Пример простой проверки
            if (isValidUser(username, password)) {
                logger.info("Authorization successful for user: $username")
                call.respondText("Some token")
            } else {
                logger.info("Authorization failed for user: $username")
                call.respondText("Authorization failed!")
            }
        }

        post("/registration") {

            val loginData = call.receive<LoginData>()
            val username = loginData.username
            val password = loginData.password

            if (userExists(username)) {
                call.respondText("This user already exists!")
            } else {
                logger.info("New user registered: $username")
                val user = User(username, password)
                addUser(user)
                call.respondText("Some token")
            }
        }

        get("/hello") { call.respondText("Hello world!") }
    }
}
