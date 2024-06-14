package com.example


import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.network.fold

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*

import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse



class TelegramBotFactory(){
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

    private suspend fun initializeAdminBot(){
        adminBotToken = getAdminTelegram()
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
    suspend fun postMessageToChannel(email: String, channelName: String, message: String, date: LocalDateTime): Boolean {
        val botToken = getTelegram(email) ?: return false
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
                ifSuccess = {  message ->
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

            val response= client.get("https://api.telegram.org/bot$botToken/getMessages") {
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



//    fun getPosts(botToken: String, channelUsername: String): List<Message> {
//        val channelId = getChannelId(botToken, channelUsername)
//        val bot = bot {
//            token = botToken
//        }
//        val botId = getBotId(botToken) ?: return emptyList()
//        val messages = mutableListOf<Message>()
//
//        runBlocking {
//            var offset: Long? = 0
//            var shouldContinue = true
//            while (shouldContinue) {
//                val result = bot.getUpdates(offset = offset, limit = 100)
//
//                result.fold(
//                    ifSuccess = { updates ->
//                        val botMessages = updates.mapNotNull { it.message }
//                     //       .filter { it.chat.id == channelId && it.from?.id == botId }
//                        messages.addAll(botMessages)
//
//                        if (updates.isEmpty()) {
//                            shouldContinue = false
//                        } else {
//                            offset = updates.last().updateId + 1
//                        }
//                    },
//                    ifError = { error ->
//                        println("Error fetching updates: $error")
//                        shouldContinue = false
//                    }
//                )
//            }
//        }
//
//        return messages
//    }

//    fun addBotToChannel(botToken: String, channelId: String): Boolean {
//        val chatId = ChatId.fromId(channelId.toLong())
//
//        // Create the bot instance
//        val bot = bot {
//            token = botToken
//        }
//        bot.startPolling()
//
//        // Get bot user id
//        val botUser: User = bot.getMe().fold(
//            ifSuccess = { it },
//            ifError = {
//                println("Error getting bot info: $it")
//                return false
//            }
//        )
//
//        // Add the bot to the channel as an administrator
//        val result = adminBot?.promoteChatMember(
//            chatId = chatId,
//            userId = botUser.id,
//            canChangeInfo = true,
//            canPostMessages = true,
//            canEditMessages = true,
//            canDeleteMessages = true,
//            canInviteUsers = true,
//            canRestrictMembers = true,
//            canPinMessages = true,
//            canPromoteMembers = true
//        )
//        bot.stopPolling()
//
//        return result?.fold(
//            ifSuccess = {
//                println("Bot successfully added as admin")
//                true
//            },
//            ifError = {
//                println("Error adding bot as admin: $it")
//                false
//            }
//        )
//            ?: false
//    }

//    fun addBotToChannel(botToken: String, channelId: String): Boolean {
//        val chatId = ChatId.fromId(channelId.toLong())
//
//        // Получение ссылки приглашения
//        val inviteLinkResult = adminBot?.createChatInviteLink(chatId)
//
//        val inviteLink = inviteLinkResult?.fold(
//            ifSuccess = { it.inviteLink },
//            ifError = {
//                println("Error creating invite link: $it")
//                null
//            }
//        )
//
//        inviteLink ?: return false
//
//        // Переход по ссылке для добавления бота в канал
//        return joinChannel(inviteLink, botToken)
//    }
//
//    private fun joinChannel(inviteLink: String, botToken: String): Boolean {
//        val client = HttpClient.newHttpClient()
//        val request = HttpRequest.newBuilder()
//            .uri(URI.create(inviteLink))
//            .header("Authorization", "Bearer $botToken")
//            .build()
//
//        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
//
//        return response.statusCode() == 200
//    }

}