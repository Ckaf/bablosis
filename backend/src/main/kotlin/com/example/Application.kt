package com.example

// import io.ktor.features.*
import com.example.plugins.*
import io.ktor.http.*
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
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


fun Application.configureSerialization() {
    install(ContentNegotiation) { json() }
}

fun Application.configureRouting() {
    val logger = LoggerFactory.getLogger("Application")
    val tf = TokenFactory()

    routing {
        post("/login") {
            val loginData = call.receive<LoginData>()
            var code = HttpStatusCode.Conflict
            var jsonResponse = ""
                if (isValidUser(loginData.username, loginData.password)) {
                logger.info("Authorization successful for user: $loginData.username")
                call.respondText(tf.genToken(loginData.username))
                    code = HttpStatusCode.OK
            } else {
                logger.info("Authorization failed for user: $loginData.username")
                jsonResponse = buildJsonObject {
                    put("error", "Authorization failed!")
                }.toString()
            }
            call.respond(code, jsonResponse)
        }

        post("/registration") {

            val loginData = call.receive<LoginData>()
            var code = HttpStatusCode.Conflict
            var jsonResponse = ""

            if (userExists(loginData.username)) {
                jsonResponse = buildJsonObject {
                    put("error", "This user already exists!")
                }.toString()
            } else {
                logger.info("New user registered: $loginData.username")
                val user = User(loginData.username, loginData.password)
                addUser(user)
                code = HttpStatusCode.OK
            }
            call.respond(code, jsonResponse)
        }

        post ("/set_role") {
            val roleSetData = call.receive<RoleSetData>()
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            if (tf.hasAccess(roleSetData.accessToken, TokenFactory.roles_enum.ADMIN)){
                confirmUser(roleSetData.username)
                if ( TokenFactory.roles_enum.ADMIN == TokenFactory.roles_enum.valueOf(roleSetData.role)) setAdmin(roleSetData.username)
                else if ( TokenFactory.roles_enum.ISHTAR == TokenFactory.roles_enum.valueOf(roleSetData.role)) setIshtar(roleSetData.username)

                logger.info("Role set for user: ${roleSetData.username}")
                code = HttpStatusCode.OK
            }
            else{
                jsonResponse = buildJsonObject {
                    put("error", "Insufficient rights for this operation!")
                }.toString()
            }
            call.respond(code, jsonResponse)
        }
        post("/add_tg") {
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val bindData = call.receive<TgBindData>()
            if(tf.hasAccess(bindData.accessToken, TokenFactory.roles_enum.USER)) {
                setTelegram(tf.getUsernameFromToken(bindData.accessToken), bindData.tg)

                logger.info("Telegram token set for user: ${tf.getUsernameFromToken(bindData.accessToken)}")
                code = HttpStatusCode.OK
            }
            else{
                jsonResponse = buildJsonObject {
                    put("error", "Insufficient rights for this operation!")
                }.toString()
            }
            call.respond(code, jsonResponse)
        }


        get("/hello") { call.respondText("Hello world!") }
    }
}
