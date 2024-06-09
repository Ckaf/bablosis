package com.example

// import io.ktor.features.*
import com.example.TokenFactory.roles_enum
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
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf.Constructor
import kotlin.time.Duration.Companion.seconds

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
    val tbf = TelegramBotFactory()

    routing {
        post("/login") {
            val loginData = call.receive<LoginData>()
            var code = HttpStatusCode.Conflict
            var jsonResponse = ""
                if (isValidUser(loginData.email, loginData.password)) {
                logger.info("Authorization successful for user: $loginData.username")
                call.respondText(tf.genToken(loginData.email))
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

            val regData = call.receive<RegData>()
            var code = HttpStatusCode.Conflict
            var jsonResponse = ""

            if (userExists(regData.email)) {
                jsonResponse = buildJsonObject {
                    put("error", "This user already exists!")
                }.toString()
            } else {
                logger.info("New user registered: $regData.email")
                val user = User(regData.username, regData.password, regData.email)
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
                confirmUser(roleSetData.email)
                if ( TokenFactory.roles_enum.ADMIN == TokenFactory.roles_enum.valueOf(roleSetData.role)) setAdmin(roleSetData.email)
                else if ( TokenFactory.roles_enum.ISHTAR == TokenFactory.roles_enum.valueOf(roleSetData.role)) setIshtar(roleSetData.email)
                else if ( TokenFactory.roles_enum.COURIER == TokenFactory.roles_enum.valueOf(roleSetData.role)) setCourier(roleSetData.email)

                logger.info("Role set for user: ${roleSetData.email}")
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
                setTelegram(tf.getEmailFromToken(bindData.accessToken), bindData.tg)

                logger.info("Telegram token set for user: ${tf.getEmailFromToken(bindData.accessToken)}")
                code = HttpStatusCode.OK
            }
            else{
                jsonResponse = buildJsonObject {
                    put("error", "Insufficient rights for this operation!")
                }.toString()
            }
            call.respond(code, jsonResponse)
        }

        post("/all_user"){
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<AccessTokenData>()
            if(tf.hasAccess(data.accessToken, TokenFactory.roles_enum.ADMIN)) {
                val users = getAllUsers()
                jsonResponse = buildJsonObject {
                    putJsonArray("person") {
                        users.forEach {
                            addJsonObject {
                                put("name", it.name)
                                put("email", it.email)
                                put("role", getUserRole(it.isAdmin, it.isIshtar, it.isCourier, it.confirmed).toString())
                            }
                        }
                    }
                }.toString()

                code = HttpStatusCode.OK
            }
            call.respond(code, jsonResponse)
        }

        post("/post"){
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<PostData>()
            if(tf.hasAccess(data.accessToken, TokenFactory.roles_enum.USER)) {
                val tg = getTelegram(tf.getEmailFromToken(data.accessToken))
                if (tg != null) {
                    if(tbf.postMessageToChannel(tg, data.channelId, data.msg, data.date)){
                        code = HttpStatusCode.OK
                    }
                    else {
                        jsonResponse = buildJsonObject {
                            put("error", "Something went wrong!")
                        }.toString()
                        code = HttpStatusCode.MethodNotAllowed
                    }
                }
                else {
                    jsonResponse = buildJsonObject {
                        put("error", "Telegram token not found!")
                    }.toString()
                    code = HttpStatusCode.BadRequest
                }

            }
            call.respond(code, jsonResponse)
        }

        get("/hello") {
            // todo drop
            val s = 1.seconds
            val currentTime = Clock.System.now().plus(s)
                .toLocalDateTime(TimeZone.currentSystemDefault())

            val time = currentTime

            tbf.postMessageToChannel(
                "mitra@ya.ru", "glam_disc", "4 msg", time)

            val post = tbf.getMessageFromChannel("mitra@ya.ru", "glam_disc", 7)
            println(post)
            call.respondText("Hello world!")
        }
    }
}
fun getUserRole(isAdmin:Boolean, isIshtar:Boolean, isCourier:Boolean, isConfirmed:Boolean): roles_enum {
    if (isAdmin) return roles_enum.ADMIN
    if (isIshtar) return roles_enum.ISHTAR
    if (isCourier) return roles_enum.COURIER
    if (isConfirmed) return roles_enum.USER
    return roles_enum.NONE
}