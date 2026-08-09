package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.GeminiModel
import com.example.data.GeminiRepository
import com.example.data.GeminiResult
import com.example.data.MessageSender
import com.example.data.local.ChatLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SasaUiState(
    val messages: List<ChatMessage> = emptyList(),
    val selectedModel: GeminiModel = GeminiModel.FLASH_3_6,
    val isGenerating: Boolean = false,
    val customApiKey: String = "",
    val activeModelTag: String = GeminiModel.FLASH_3_6.displayName,
    val systemNotice: String? = null,
    val showApiKeyDialog: Boolean = false
)

class SasaViewModel(
    private val repository: GeminiRepository = GeminiRepository(),
    private val localRepository: ChatLocalRepository? = null
) : ViewModel() {

    private val defaultWelcomeMessage = ChatMessage(
        sender = MessageSender.SASA_AI,
        text = "مرحباً بك! أنا منظومة صاصا AI (Sasa AI v15.2).\n" +
                "المساعد الذكي للتحليل والبرمجة والتطوير باللغة العربية.\n\n" +
                "💡 متصل مباشرة بنماذج Gemini (3.6 Flash, 3.5 Flash-Lite, 3.1 Pro, تفكير موسّع).\n\n" +
                "كيف يمكنني مساعدتك اليوم؟",
        modelUsed = GeminiModel.FLASH_3_6.displayName
    )

    private val _uiState = MutableStateFlow(
        SasaUiState(messages = listOf(defaultWelcomeMessage))
    )
    val uiState: StateFlow<SasaUiState> = _uiState.asStateFlow()

    init {
        observeSavedMessages()
    }

    private fun observeSavedMessages() {
        localRepository?.let { repo ->
            viewModelScope.launch {
                repo.allMessages.collect { savedMessages ->
                    if (savedMessages.isEmpty()) {
                        repo.saveMessage(defaultWelcomeMessage)
                    } else {
                        _uiState.value = _uiState.value.copy(messages = savedMessages)
                    }
                }
            }
        }
    }

    fun onSendMessage(inputPrompt: String) {
        val prompt = inputPrompt.trim()
        if (prompt.isBlank() || _uiState.value.isGenerating) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = prompt
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            isGenerating = true,
            systemNotice = null
        )

        // Save user message to local Room DB
        viewModelScope.launch {
            localRepository?.saveMessage(userMessage)
        }

        viewModelScope.launch {
            val result = repository.generateContentWithFailover(
                prompt = prompt,
                conversationHistory = updatedMessages,
                preferredModel = _uiState.value.selectedModel,
                customApiKey = _uiState.value.customApiKey
            )

            val aiMessage = when (result) {
                is GeminiResult.Success -> {
                    ChatMessage(
                        sender = MessageSender.SASA_AI,
                        text = result.text,
                        modelUsed = result.modelUsed.displayName
                    )
                }
                is GeminiResult.QuotaExceeded -> {
                    ChatMessage(
                        sender = MessageSender.SYSTEM,
                        text = "⚠️ تنبيه قيود الاستخدام: ${result.message}\nجارٍ تحويل الطلب تلقائياً...",
                        isSystemNotice = true
                    )
                }
                is GeminiResult.Error -> {
                    ChatMessage(
                        sender = MessageSender.SASA_AI,
                        text = "❌ تعذر إكمال الاتصال بنموذج الذكاء الاصطناعي:\n${result.message}",
                        isError = true
                    )
                }
            }

            val newActiveTag = if (result is GeminiResult.Success) result.modelUsed.displayName else _uiState.value.activeModelTag
            val systemNoticeText = if (result is GeminiResult.Error) result.message else null

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMessage,
                isGenerating = false,
                activeModelTag = newActiveTag,
                systemNotice = systemNoticeText
            )

            // Save AI response message to Room DB
            localRepository?.saveMessage(aiMessage)
        }
    }

    fun onSelectModel(model: GeminiModel) {
        _uiState.value = _uiState.value.copy(
            selectedModel = model,
            activeModelTag = model.displayName
        )
    }

    fun onSaveCustomApiKey(key: String) {
        _uiState.value = _uiState.value.copy(
            customApiKey = key.trim(),
            showApiKeyDialog = false,
            systemNotice = "تم حفظ مفتاح API بنجاح!"
        )
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = show)
    }

    fun onClearChat() {
        val resetMessage = ChatMessage(
            sender = MessageSender.SASA_AI,
            text = "تم البدء في محادثة جديدة. يسعدني مساعدتك!",
            modelUsed = _uiState.value.selectedModel.displayName
        )
        viewModelScope.launch {
            localRepository?.clearHistory()
            localRepository?.saveMessage(resetMessage)
        }
        _uiState.value = _uiState.value.copy(
            messages = listOf(resetMessage),
            systemNotice = null
        )
    }

    fun dismissSystemNotice() {
        _uiState.value = _uiState.value.copy(systemNotice = null)
    }

    class Factory(
        private val localRepository: ChatLocalRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SasaViewModel(localRepository = localRepository) as T
        }
    }
}

