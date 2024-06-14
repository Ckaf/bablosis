
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgresql_driver_version: String by project
val exposed_version: String by project

plugins {
    kotlin("jvm") version "1.9.24"
    id("io.ktor.plugin") version "2.3.11"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.postgresql:postgresql:$postgresql_driver_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")
    implementation("org.postgresql:postgresql:42.5.1")
    implementation("com.h2database:h2:2.1.214")
    implementation("io.ktor:ktor-server-metrics-jvm")
    implementation("io.ktor:ktor-server-caching-headers-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml:2.3.11")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation ("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation ("io.jsonwebtoken:jjwt-impl:0.11.2")
    implementation ("io.jsonwebtoken:jjwt-jackson:0.11.2")
    implementation("com.github.badoualy:kotlogram:1.0.0-RC3")

    implementation("com.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.1.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
}
