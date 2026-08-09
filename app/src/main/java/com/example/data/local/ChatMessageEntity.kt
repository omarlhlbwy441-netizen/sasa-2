package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.ChatMessage
import com.example.data.MessageSender

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val modelUsed: String? = null,
    val isError: Boolean = false,
    val isSystemNotice: Boolean = false
) {
    fun toDomain(): ChatMessage {
        val senderEnum = try {
            MessageSender.valueOf(sender)
        } catch (e: Exception) {
            MessageSender.SASA_AI
        }
        return ChatMessage(
            id = id,
            sender = senderEnum,
            text = text,
            timestamp = timestamp,
            modelUsed = modelUsed,
            isError = isError,
            isSystemNotice = isSystemNotice
        )
    }

    companion object {
        fun fromDomain(message: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = message.id,
                sender = message.sender.name,
                text = message.text,
                timestamp = message.timestamp,
                modelUsed = message.modelUsed,
                isError = message.isError,
                isSystemNotice = message.isSystemNotice
            )
        }
    }
}
