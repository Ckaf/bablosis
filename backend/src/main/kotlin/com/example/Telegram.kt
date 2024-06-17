package com.example


import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.network.Response
import com.github.kotlintelegrambot.network.fold

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*

import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.json


class TelegramBotFactory() {
    private var adminBotToken: String? = null
    private var adminBot: Bot? = null
    private var channelMap = hashMapOf<String, HashMap<String, String>>()

    init {
        runBlocking {
            initializeChannelMap()
            initializeAdminBot()
        }
    }

    private suspend fun initializeChannelMap() {
        val channelNames = getAllChannelNames()
        channelNames.forEach { channelName ->
            val botTokens = getBotTokensByChannelName(channelName)
            val botTokenMap = hashMapOf<String, String>()
            botTokens.forEachIndexed { index, token ->
                if (token != null) {
                    botTokenMap[token] = "bot$index"
                }
            }
            channelMap[channelName] = botTokenMap
        }
    }

    private suspend fun initializeAdminBot() {
        adminBotToken = Config.adminBotToken
        adminBot = bot {
            token = adminBotToken.toString()
            dispatch {
                text {
                    println("Received message: ${message.text}")
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun postMessageToChannel(
        email: String,
        channelName: String,
        message: String,
        date: LocalDateTime
    ): Boolean {
        val botToken = Config.adminBotToken
        // val deferredMessageId = CompletableDeferred<Long?>()
        val bot = bot {
            token = botToken
        }

        val chatId = ChatId.fromChannelUsername(channelName)

        // Вычисляем задержку до указанного времени
        val now = Clock.System.now()
        val targetTime = date.toInstant(TimeZone.currentSystemDefault())
        val delayMillis = targetTime.toEpochMilliseconds() - now.toEpochMilliseconds()

        // Проверяем, что задержка положительная
        if (delayMillis <= 0) {
            println("Specified time is in the past. Message will not be sent.")
            return false
        }

        GlobalScope.launch {
            delay(delayMillis)
            val result = bot.sendMessage(chatId, message)

            result.fold(
                ifSuccess = { message ->
                    println("Message successfully sent to channel")
                    addPost(email, channelName, message.messageId)
                    // deferredMessageId.complete(message.messageId)
                },
                ifError = {
                    println("Error sending message to channel: $it")
                }
            )
        }

        return true
    }

    fun getChannelId(botToken: String, channelUsername: String): Long? {
        val bot = bot {
            token = botToken
        }

        var chatId: Long? = null

        runBlocking {
            val result = bot.getChat(ChatId.fromChannelUsername(channelUsername))

            result.fold(
                ifSuccess = { chat ->
                    chatId = chat.id
                },
                ifError = { error ->
                    println("Error fetching chat info: $error")
                }
            )
        }

        return chatId
    }

    fun getChannelMemberCount(botToken: String, channelUsername: String): Int? {
        val bot = bot {
            token = botToken
        }

        val chatId = getChannelId(botToken, channelUsername)

        return if (chatId != null) {
            var memberCount: Int? = null
            runBlocking {
                val response = bot.getChatMemberCount(ChatId.fromId(chatId))
                response.fold(
                    { result ->
                        if (result != null) {
                            memberCount = result.result
                        }
                    },
                    { error ->
                        println("Error: ${error.errorBody}")
                    }
                )
            }
            memberCount
        } else {
            null
        }
    }

    fun getBotId(botToken: String): Long? {
        val bot = bot {
            token = botToken
        }

        return runBlocking {
            val me = bot.getMe().getOrNull()
            me?.id
        }
    }

//    fun getMessages(channel: String, messageId: Int, limit: Int, token: String): List<String> {
//        val url = "https://api.telegram.org/bot$token/channels.getMessages?channel=$channel&limit=$limit&message_id=$messageId"
//        val client = HttpClient()
//        val response = get(url)
//        if (response.statusCode == 200) {
//            val messages = response.jsonObject.getJSONArray("result")
//            return messages.toList().map { it.toString() }
//        } else {
//            throw Exception("Failed to fetch messages. Status code: ${response.statusCode}")
//        }
//    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }


    fun getMessageFromChannel(email: String, channelUsername: String, messageId: Long): Message? {
        return runBlocking {
            val botToken = getTelegram(email) ?: return@runBlocking null
            val chatId = getChannelId(botToken, channelUsername) ?: return@runBlocking null

            val response = client.get("https://api.telegram.org/bot$botToken/getMessages") {
                parameter("channel", chatId)
                parameter("id", messageId)
            }

            if (response.status.value == 200) {
                val getMessageResponse: GetMessageResponse = response.body()
                if (getMessageResponse.ok) {
                    return@runBlocking getMessageResponse.result
                }
            }
            null
        }
    }

}
