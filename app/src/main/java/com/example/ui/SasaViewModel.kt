package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.GeminiModel
import com.example.data.GeminiRepository
import com.example.data.GeminiResult
import com.example.data.GitHubFileContent
import com.example.data.GitHubRepoItem
import com.example.data.GitHubRepository
import com.example.data.GitHubResult
import com.example.data.GitHubTreeItem
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
    val showApiKeyDialog: Boolean = false,
    
    // GitHub Integration State
    val githubToken: String = "",
    val githubUserStatus: String? = null,
    val showGitHubDialog: Boolean = false,
    val githubRepos: List<GitHubRepoItem> = emptyList(),
    val selectedRepo: GitHubRepoItem? = null,
    val repoTree: List<GitHubTreeItem> = emptyList(),
    val selectedFile: GitHubFileContent? = null,
    val isLoadingGitHub: Boolean = false
)

class SasaViewModel(
    private val repository: GeminiRepository = GeminiRepository(),
    private val githubRepo: GitHubRepository = GitHubRepository(),
    private val localRepository: ChatLocalRepository? = null
) : ViewModel() {

    private val defaultWelcomeMessage = ChatMessage(
        sender = MessageSender.SASA_AI,
        text = "مرحباً بك! أنا منظومة صاصا AI (Sasa AI v15.2).\n" +
                "المساعد الذكي للتحليل والبرمجة والتطوير باللغة العربية.\n\n" +
                "💡 متصل مباشرة بنماذج Gemini ومدعوم بإدارة مستودعات GitHub المباشرة (فحص، تعديل، Commit، ونسخ).\n\n" +
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

    // GitHub Integration Functions
    fun setShowGitHubDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showGitHubDialog = show)
    }

    fun setGitHubToken(token: String) {
        val cleanToken = token.trim()
        _uiState.value = _uiState.value.copy(githubToken = cleanToken, isLoadingGitHub = true)
        
        viewModelScope.launch {
            val result = githubRepo.verifyToken(cleanToken)
            when (result) {
                is GitHubResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        githubUserStatus = result.data,
                        isLoadingGitHub = false,
                        systemNotice = "تم ربط توكن GitHub بنجاح!"
                    )
                    loadGitHubRepos()
                }
                is GitHubResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        githubUserStatus = null,
                        isLoadingGitHub = false,
                        systemNotice = result.message
                    )
                }
            }
        }
    }

    fun loadGitHubRepos() {
        val token = _uiState.value.githubToken
        if (token.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoadingGitHub = true)
        viewModelScope.launch {
            when (val res = githubRepo.getUserRepos(token)) {
                is GitHubResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        githubRepos = res.data,
                        isLoadingGitHub = false
                    )
                }
                is GitHubResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGitHub = false,
                        systemNotice = res.message
                    )
                }
            }
        }
    }

    fun inspectRepoTree(owner: String, repo: String, branch: String = "main") {
        val token = _uiState.value.githubToken
        _uiState.value = _uiState.value.copy(isLoadingGitHub = true)

        viewModelScope.launch {
            when (val res = githubRepo.getRepoTree(token, owner, repo, branch)) {
                is GitHubResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        repoTree = res.data,
                        isLoadingGitHub = false,
                        selectedFile = null
                    )
                }
                is GitHubResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGitHub = false,
                        systemNotice = res.message
                    )
                }
            }
        }
    }

    fun openRepoFile(owner: String, repo: String, path: String, branch: String = "main") {
        val token = _uiState.value.githubToken
        _uiState.value = _uiState.value.copy(isLoadingGitHub = true)

        viewModelScope.launch {
            when (val res = githubRepo.getFileContent(token, owner, repo, path, branch)) {
                is GitHubResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        selectedFile = res.data,
                        isLoadingGitHub = false
                    )
                }
                is GitHubResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGitHub = false,
                        systemNotice = res.message
                    )
                }
            }
        }
    }

    fun commitFileChanges(
        owner: String,
        repo: String,
        path: String,
        newContent: String,
        message: String,
        sha: String?,
        branch: String = "main"
    ) {
        val token = _uiState.value.githubToken
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(systemNotice = "يرجى إدخال GitHub PAT أولاً!")
            return
        }

        _uiState.value = _uiState.value.copy(isLoadingGitHub = true)
        viewModelScope.launch {
            when (val res = githubRepo.commitFileChange(token, owner, repo, path, newContent, message, sha, branch)) {
                is GitHubResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGitHub = false,
                        systemNotice = res.data
                    )
                    // Refresh file content
                    openRepoFile(owner, repo, path, branch)
                }
                is GitHubResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGitHub = false,
                        systemNotice = res.message
                    )
                }
            }
        }
    }

    fun forkRepo(owner: String, repo: String) {
        val token = _uiState.value.githubToken
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(systemNotice = "يرجى توفير توكن GitHub للنسخ!")
            return
        }

        _uiState.value = _uiState.value.copy(isLoadingGitHub = true)
        viewModelScope.launch {
            when (val res = githubRepo.forkRepo(token, owner, repo)) {
                is GitHubResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGitHub = false,
                        systemNotice = res.data
                    )
                    loadGitHubRepos()
                }
                is GitHubResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingGitHub = false,
                        systemNotice = res.message
                    )
                }
            }
        }
    }

    fun setSelectedRepo(repo: GitHubRepoItem?) {
        _uiState.value = _uiState.value.copy(selectedRepo = repo, repoTree = emptyList(), selectedFile = null)
        if (repo != null) {
            val parts = repo.fullName.split("/")
            if (parts.size == 2) {
                inspectRepoTree(parts[0], parts[1], repo.defaultBranch)
            }
        }
    }

    fun onSendMessage(inputPrompt: String) {
        val prompt = inputPrompt.trim()
        if (prompt.isBlank() || _uiState.value.isGenerating) return

        val existingHistory = _uiState.value.messages
        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = prompt
        )

        val updatedMessages = existingHistory + userMessage
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
            // Check for GitHub token in prompt and save it automatically
            val tokenRegex = Regex("""(ghp_[a-zA-Z0-9]{36,40}|github_pat_[a-zA-Z0-9_]{80,90})""")
            val extractedToken = tokenRegex.find(prompt)?.value
            if (!extractedToken.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(githubToken = extractedToken)
            }

            // Auto fetch GitHub link or token context if present in prompt
            var enrichedPrompt = prompt
            val githubContext = githubRepo.resolveGitHubContext(prompt, _uiState.value.githubToken)
            if (!githubContext.isNullOrBlank()) {
                enrichedPrompt = "$prompt\n\n$githubContext"
            }

            val result = repository.generateContentWithFailover(
                prompt = enrichedPrompt,
                conversationHistory = existingHistory,
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

