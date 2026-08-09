package com.example.data

enum class GeminiModel(
    val id: String,
    val displayName: String,
    val description: String
) {
    FLASH_3_6("gemini-2.0-flash", "3.6 Flash", "مساعدة متكاملة، البرمجة والتحليل اليومي"),
    FLASH_LITE_3_5("gemini-2.0-flash-lite", "3.5 Flash-Lite", "أسرع الإجابات والردود الفورية"),
    PRO_3_1("gemini-1.5-pro", "3.1 Pro", "الرياضيات والبرمجة المتقدمة والتحليل العميق"),
    THINKING_EXP("gemini-2.0-flash-thinking-exp", "تفكير موسّع", "حل المشاكل المعقدة والتخطيط البرمجي المنطقي")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val isError: Boolean = false,
    val isSystemNotice: Boolean = false
)

enum class MessageSender {
    USER,
    SASA_AI,
    SYSTEM
}

data class ApiKeyStatus(
    val isCustom: Boolean,
    val keyPreview: String
)
