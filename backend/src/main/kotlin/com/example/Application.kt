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
    val proto = MTProto()

    routing {
        post("/login") {
            val loginData = call.receive<LoginData>()
            var code = HttpStatusCode.Conflict
            var jsonResponse = ""
                if (isValidUser(loginData.email, loginData.password)) {
                logger.info("Authorization successful for user: $loginData.username")
                    val token = tf.genToken(loginData.email)
                    jsonResponse = buildJsonObject {
                        put("name", getName(loginData.email))
                        put("role", getUserRole(loginData.email).toString())
                    }.toString()
                    call.response.headers.append("Authorization", "Bearer $token")
                    code = HttpStatusCode.OK
            } else {
                logger.info("Authorization failed for user: $loginData.username")
                jsonResponse = buildJsonObject {
                    put("error", "Authorization failed!")
                }.toString()
            }
            // todo put role data and other
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
            if (tf.hasAccess(roleSetData.accessToken, roles_enum.ADMIN)){
                confirmUser(roleSetData.email)
                if ( roles_enum.ADMIN == roles_enum.valueOf(roleSetData.role)) setAdmin(roleSetData.email)
                else if ( roles_enum.ISHTAR == roles_enum.valueOf(roleSetData.role)) setIshtar(roleSetData.email)
                else if ( roles_enum.COURIER == roles_enum.valueOf(roleSetData.role)) setCourier(roleSetData.email)
                else {
                getId(roleSetData.email)?.value?.let { it1 -> initBalance(it1) }
                }
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
            if(tf.hasAccess(bindData.accessToken, roles_enum.USER)) {
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
            if(tf.hasAccess(data.accessToken, roles_enum.ADMIN)) {
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

        post("/post") {
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<PostData>()
            if (tf.hasAccess(data.accessToken, roles_enum.USER)) {
                val tg = getTelegram(tf.getEmailFromToken(data.accessToken))
                if (tg != null) {
                    if (tbf.postMessageToChannel(tg, data.channelId, data.msg, data.date)) {
                        code = HttpStatusCode.OK
                    } else {
                        jsonResponse = buildJsonObject {
                            put("error", "Something went wrong!")
                        }.toString()
                        code = HttpStatusCode.MethodNotAllowed
                    }
                } else {
                    jsonResponse = buildJsonObject {
                        put("error", "Telegram token not found!")
                    }.toString()
                    code = HttpStatusCode.BadRequest
                }

            }
            call.respond(code, jsonResponse)

        }

        post("/create_order"){
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<OrderCreateData>()
            if (tf.hasAccess(data.accessToken, roles_enum.USER)) {
                val id = getId(tf.getEmailFromToken(data.accessToken))?.value!!
                if (changeBalanceToUser(id,data.bablos)) {
                    addOrder(id, null, data.address, data.bablos, status_enum.NONE.toString())
                    code = HttpStatusCode.OK
                }
                else {
                    jsonResponse = buildJsonObject {
                        put("error", "The balance is less than the requested amount!")
                    }.toString()
                    code = HttpStatusCode.MethodNotAllowed
                }
            }
            call.respond(code, jsonResponse)
        }

        post("/get_my_orders") {
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<AccessTokenData>()
            val id = getId(tf.getEmailFromToken(data.accessToken))?.value!!

            if (tf.hasAccess(data.accessToken, roles_enum.USER)) {
                val orders = getOrdersByUserId(id)
                jsonResponse = buildJsonObject {
                    putJsonArray("orders") {
                        orders.forEach {
                            addJsonObject {
                                put("id", it.id)
                                put("bablos", it.bablos)
                                put("address", it.address)
                                put("status", it.status.toString())
                            }
                        }
                    }
                }.toString()
                code = HttpStatusCode.OK
            } else if (tf.hasAccess(data.accessToken, roles_enum.COURIER)) {
                val orders = getOrdersByCourierId(id)
                jsonResponse = buildJsonObject {
                    putJsonArray("orders") {
                        orders.forEach {
                            addJsonObject {
                                put("id", it.id)
                                put("bablos", it.bablos)
                                put("address", it.address)
                                put("status", it.status.toString())
                            }
                        }
                    }
                }.toString()
                code = HttpStatusCode.OK
            }
            call.respond(code, jsonResponse)
        }

        post("/choose_order"){
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<OrderData>()
            if (tf.hasAccess(data.accessToken, roles_enum.COURIER)) {
                val id = getId(tf.getEmailFromToken(data.accessToken))?.value!!
                updateOrderCourier(data.orderId, id)
                updateOrderStatus(data.orderId, status_enum.ASSEMBLY)
                code = HttpStatusCode.OK
            }
            call.respond(code, jsonResponse)
        }

        post("/set_order_status"){
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<OrderData>()
            if (tf.hasAccess(data.accessToken, roles_enum.COURIER)) {
                when (getOrderStatus(data.orderId)){
                    status_enum.NONE -> code = HttpStatusCode.Conflict
                    status_enum.ASSEMBLY ->{
                        if (data.status == status_enum.DELIVERY) {
                            updateOrderStatus(data.orderId, data.status)
                            code = HttpStatusCode.OK
                        }
                        else code = HttpStatusCode.Conflict
                    }
                    status_enum.DELIVERY ->{
                        if (data.status == status_enum.DONE) {
                            updateOrderStatus(data.orderId, data.status)
                            code = HttpStatusCode.OK
                        }
                        else code = HttpStatusCode.Conflict
                }
                    status_enum.DONE -> code = HttpStatusCode.Conflict
                    else -> HttpStatusCode.BadRequest
                }
            }
            call.respond(code, jsonResponse)
        }

        post("/get_free_orders"){
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<AccessTokenData>()
            if (tf.hasAccess(data.accessToken, roles_enum.COURIER)) {
                val orders = getFreeOrders()
                jsonResponse = buildJsonObject {
                    putJsonArray("orders") {
                        orders.forEach {
                            addJsonObject {
                                put("bablos", it.bablos)
                                put("address", it.address)
                                put("status", it.status.toString())
                            }
                        }
                    }
                }.toString()
                code = HttpStatusCode.OK
            }
            call.respond(code, jsonResponse)
        }

        post("/set_bablos"){
            var code = HttpStatusCode.Forbidden
            var jsonResponse = ""
            val data = call.receive<BablosData>()
            if (tf.hasAccess(data.accessToken, roles_enum.ISHTAR)) {
                val users = getAllUsers()
                TODO()
                users.forEach({})

                code = HttpStatusCode.OK
            }
            call.respond(code, jsonResponse)
        }

        get("/hello") {
            // todo drop
//            val s = 1.seconds
//            val currentTime = Clock.System.now().plus(s)
//                .toLocalDateTime(TimeZone.currentSystemDefault())
//
//            val time = currentTime
//
//            tbf.postMessageToChannel(
//                "mitra@ya.ru", "glam_disc", "4 msg", time)
//
//            val post = tbf.getMessageFromChannel("mitra@ya.ru", "glam_disc", 7)
//            println(post)


            call.respondText("Hello world!")
        }
    }
}
suspend fun getUserRole(email:String): roles_enum{
    val a = isUserAdmin(email)
    val i = isUserIshtar(email)
    val cou = isUserCourier(email)
    val con = isUserConfirmed(email)
    return getUserRole(a, i, cou, con)
}

fun getUserRole(isAdmin:Boolean, isIshtar:Boolean, isCourier:Boolean, isConfirmed:Boolean): roles_enum {
    if (isAdmin) return roles_enum.ADMIN
    if (isIshtar) return roles_enum.ISHTAR
    if (isCourier) return roles_enum.COURIER
    if (isConfirmed) return roles_enum.USER
    return roles_enum.NONE
}