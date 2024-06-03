package com.example

import kotlinx.coroutines.runBlocking


import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ChatMember
import com.github.kotlintelegrambot.entities.ChatPermissions
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.network.fold
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