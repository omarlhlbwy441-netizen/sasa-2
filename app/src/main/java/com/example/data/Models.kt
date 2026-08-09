package com.example.data

enum class GeminiModel(
    val id: String,
    val displayName: String,
    val description: String
) {
    FLASH_2_0("gemini-2.0-flash", "Gemini 2.0 Flash", "النموذج الأسرع والأحدث كفاءة للمهام العامة والبرمجة"),
    FLASH_1_5("gemini-1.5-flash", "Gemini 1.5 Flash", "نموذج الأداء العالي مع نافذة سياق ضخمة"),
    PRO_1_5("gemini-1.5-pro", "Gemini 1.5 Pro", "نموذج البرمجة المتقدمة والتفكير المنطقي المعقد"),
    FLASH_1_5_8B("gemini-1.5-flash-8b", "Gemini 1.5 Flash 8B", "نموذج المهام السريعة والإجابات المباشرة")
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
