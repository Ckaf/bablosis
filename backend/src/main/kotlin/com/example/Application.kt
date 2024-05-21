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
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting()
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

            // val username = call.request.queryParameters["username"]
            // val password = call.request.queryParameters["password"]

            logger.info("GET SOME RESPONSE: $username")
            println("GET SOME RESPONSE: $username")
            // Пример простой проверки
            if (username == "admin" && password == "admin123") {
                logger.info("Authorization successful for user: $username")
                call.respondText("Authorization successful!")
            } else {
                logger.info("Authorization failed for user: $username")
                call.respondText("Authorization failed!")
            }
        }
        get("/hello") { call.respondText("Hello world!") }
    }
}
