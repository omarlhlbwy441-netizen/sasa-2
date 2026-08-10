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

import com.example.data.FileGeneratorRepository
import com.example.data.GeneratedFile
import com.example.data.MediaProcessingRepository
import com.example.data.CodeFixRepository
import com.example.data.CodeAutoFixResponse
import com.example.data.RepoScanFixResponse
import com.example.data.EnvironmentEvolutionResponse

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
    val isLoadingGitHub: Boolean = false,
    val isBuildingOrPushing: Boolean = false,
    val activeMediaTask: String? = null
)

class SasaViewModel(
    private val repository: GeminiRepository = GeminiRepository(),
    private val githubRepo: GitHubRepository = GitHubRepository(),
    private val fileGeneratorRepo: FileGeneratorRepository = FileGeneratorRepository(),
    private val mediaProcessingRepo: MediaProcessingRepository = MediaProcessingRepository(),
    private val codeFixRepo: CodeFixRepository = CodeFixRepository(),
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
            try {
                // Check for GitHub token in prompt and save it automatically
                val tokenRegex = Regex("""(ghp_[a-zA-Z0-9]{36,40}|github_pat_[a-zA-Z0-9_]{80,90})""")
                val extractedToken = tokenRegex.find(prompt)?.value
                if (!extractedToken.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(githubToken = extractedToken)
                }

                // Auto fetch GitHub link or token context if present in prompt
                var enrichedPrompt = prompt
                val githubContext = try {
                    githubRepo.resolveGitHubContext(prompt, _uiState.value.githubToken)
                } catch (e: Exception) {
                    null
                }
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
                            modelUsed = result.modelUsed.displayName,
                            codeBlocks = result.codeBlocks,
                            generatedFiles = result.generatedFiles
                        )
                    }
                    is GeminiResult.QuotaExceeded -> {
                        ChatMessage(
                            sender = MessageSender.SYSTEM,
                            text = "⚠️ تنبيه قيود الاستخدام: ${result.message}\nتم التبديل والتحويل التلقائي بنجاح.",
                            isSystemNotice = true
                        )
                    }
                    is GeminiResult.Error -> {
                        ChatMessage(
                            sender = MessageSender.SASA_AI,
                            text = "❌ تعذر إكمال الاتصال بالنموذج:\n${result.message}",
                            isError = true
                        )
                    }
                }

                val newActiveTag = if (result is GeminiResult.Success) result.modelUsed.displayName else _uiState.value.activeModelTag
                val systemNoticeText = if (result is GeminiResult.Error) result.message else null

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    activeModelTag = newActiveTag,
                    systemNotice = systemNoticeText
                )

                // Save AI response message to Room DB
                localRepository?.saveMessage(aiMessage)
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    sender = MessageSender.SASA_AI,
                    text = "⚠️ حدث خطأ غير متوقع أثناء المعالجة: ${e.message ?: "يرجى إعادت المحاولة"}",
                    isError = true
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage
                )
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
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

    fun pushUpdateToCloudRepo(
        filePath: String,
        content: String,
        commitMessage: String = "تحديث تلقائي من صاصا AI"
    ) {
        val token = _uiState.value.githubToken
        val repo = _uiState.value.selectedRepo
        val owner = repo?.fullName?.split("/")?.getOrNull(0) ?: "omarlhlbwy441-netizen"
        val repoName = repo?.fullName?.split("/")?.getOrNull(1) ?: "sasa"

        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(systemNotice = "⚠️ يرجى توفير توكن GitHub لرفع التحديثات للسحاب.")
            return
        }

        _uiState.value = _uiState.value.copy(isBuildingOrPushing = true)
        viewModelScope.launch {
            val result = fileGeneratorRepo.pushFileToCloudRepo(
                githubToken = token,
                owner = owner,
                repo = repoName,
                filePath = filePath,
                content = content,
                commitMessage = commitMessage
            )

            result.onSuccess { msg ->
                val sysMsg = ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = "☁️ [خدمة خلفية] $msg",
                    isSystemNotice = true
                )
                _uiState.value = _uiState.value.copy(
                    isBuildingOrPushing = false,
                    messages = _uiState.value.messages + sysMsg,
                    systemNotice = msg
                )
                localRepository?.saveMessage(sysMsg)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isBuildingOrPushing = false,
                    systemNotice = "❌ تعذر رفع التحديث إلى المستودع السحابي: ${err.message}"
                )
            }
        }
    }

    fun generateMediaBackground(
        prompt: String,
        mediaType: String = "IMAGE",
        style: String = "modern"
    ) {
        _uiState.value = _uiState.value.copy(activeMediaTask = "توليد وسائط خلفية: $mediaType")
        viewModelScope.launch {
            mediaProcessingRepo.generateMediaTransparently(
                prompt = prompt,
                mediaType = mediaType,
                style = style
            ) { result ->
                val mediaNotice = if (result.success) {
                    "🎨 [خدمة خلفية شفافة] تم توليد الوسائط بنجاح: ${result.description}"
                } else {
                    "⚠️ [خدمة خلفية] فشل توليد الوسائط: ${result.message}"
                }
                
                val sysMsg = ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = mediaNotice,
                    isSystemNotice = true
                )
                
                _uiState.value = _uiState.value.copy(
                    activeMediaTask = null,
                    messages = _uiState.value.messages + sysMsg,
                    systemNotice = mediaNotice
                )
                
                viewModelScope.launch {
                    localRepository?.saveMessage(sysMsg)
                }
            }
        }
    }

    fun processMediaBackground(
        operation: String,
        mediaBase64: String
    ) {
        _uiState.value = _uiState.value.copy(activeMediaTask = "معالجة وسائط خلفية: $operation")
        viewModelScope.launch {
            mediaProcessingRepo.processMediaTransparently(
                operation = operation,
                mediaBase64 = mediaBase64
            ) { result ->
                val notice = if (result.success) {
                    "📷 [خدمة خلفية شفافة] ${result.extractedText ?: "تمت معالجة الوسائط بنجاح"}"
                } else {
                    "⚠️ [خدمة خلفية] تعذرت معالجة الوسائط"
                }

                val sysMsg = ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = notice,
                    isSystemNotice = true
                )

                _uiState.value = _uiState.value.copy(
                    activeMediaTask = null,
                    messages = _uiState.value.messages + sysMsg,
                    systemNotice = notice
                )

                viewModelScope.launch {
                    localRepository?.saveMessage(sysMsg)
                }
            }
        }
    }

    fun autoFixCodeBackground(
        code: String,
        language: String = "auto",
        errorLog: String? = null,
        filename: String = "source_file"
    ) {
        viewModelScope.launch {
            codeFixRepo.autoFixCodeTransparently(
                code = code,
                language = language,
                errorLog = errorLog,
                filename = filename
            ) { result ->
                val notice = if (result.success) {
                    "🛠️ [خدمة خلفية شفافة] تم تصحيح كود $filename ($language) بنجاح: ${result.explanation}"
                } else {
                    "⚠️ [خدمة خلفية] تعذر تصحيح الكود"
                }
                val sysMsg = ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = notice,
                    isSystemNotice = true
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + sysMsg,
                    systemNotice = notice
                )
                viewModelScope.launch {
                    localRepository?.saveMessage(sysMsg)
                }
            }
        }
    }

    fun scanAndFixRepoBackground(
        owner: String,
        repo: String
    ) {
        val token = _uiState.value.githubToken.ifBlank { null }
        viewModelScope.launch {
            codeFixRepo.scanAndFixRepoTransparently(
                owner = owner,
                repo = repo,
                githubToken = token
            ) { result ->
                val notice = if (result.success) {
                    "🧹 [خدمة خلفية شفافة] تم فحص وتصحيح المستودع $owner/$repo بنجاح (${result.fixedFilesCount} ملف إصلاح)"
                } else {
                    "⚠️ [خدمة خلفية] فشل تصحيح المستودع: ${result.message}"
                }
                val sysMsg = ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = notice,
                    isSystemNotice = true
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + sysMsg,
                    systemNotice = notice
                )
                viewModelScope.launch {
                    localRepository?.saveMessage(sysMsg)
                }
            }
        }
    }

    fun evolveEnvironmentBackground(
        targetCapability: String
    ) {
        viewModelScope.launch {
            codeFixRepo.evolveEnvironmentTransparently(
                targetCapability = targetCapability
            ) { result ->
                val notice = if (result.success) {
                    "🚀 [خدمة خلفية شفافة] تم ترقية وتطوير بيئة العمل إلى النسخة ${result.environmentVersion}"
                } else {
                    "⚠️ [خدمة خلفية] فشلت ترقية البيئة"
                }
                val sysMsg = ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = notice,
                    isSystemNotice = true
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + sysMsg,
                    systemNotice = notice
                )
                viewModelScope.launch {
                    localRepository?.saveMessage(sysMsg)
                }
            }
        }
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

