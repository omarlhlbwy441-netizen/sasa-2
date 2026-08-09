package com.example.data.local

import com.example.data.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatLocalRepository(private val chatDao: ChatDao) {

    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(ChatMessageEntity.fromDomain(message))
    }

    suspend fun saveMessages(messages: List<ChatMessage>) {
        chatDao.insertMessages(messages.map { ChatMessageEntity.fromDomain(it) })
    }

    suspend fun deleteMessage(id: String) {
        chatDao.deleteMessageById(id)
    }

    suspend fun clearHistory() {
        chatDao.clearAllMessages()
    }
}
