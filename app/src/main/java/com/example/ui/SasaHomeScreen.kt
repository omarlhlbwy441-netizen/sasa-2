package com.example.ui

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import com.example.ui.components.PreviewDialog
import com.example.ui.components.CloudWorkspaceSettingsDialog
import com.example.ui.components.MemoryDialog
import com.example.ui.components.VoiceCallDialog
import com.example.ui.components.GitHubManagerDialog
import com.example.ui.components.PlansDialog
import com.example.ui.components.ProfileDialog
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Add
import com.example.ui.creative_studio.CreativeStudioScreen
import com.example.ui.neama.NeamaDomainsHubDialog
import com.example.ui.domains.NeamaDomainsScreen
import com.example.ui.sync.NeamaSovereignSyncScreen
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.GeminiModel
import com.example.data.MessageSender
import com.example.ui.components.MessageTextWithCodeBlocks
import com.example.ui.theme.SasaAccentGreen
import com.example.ui.theme.SasaAiBubble
import com.example.ui.theme.SasaCardBackground
import com.example.ui.theme.SasaDarkBackground
import com.example.ui.theme.SasaDarkSurface
import com.example.ui.theme.SasaPrimary
import com.example.ui.theme.SasaPrimaryContainer
import com.example.ui.theme.SasaSecondary
import com.example.ui.theme.SasaTextSecondary
import com.example.ui.theme.SasaUserBubble
import kotlinx.coroutines.launch
import java.util.Locale

enum class NeamaNavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CHAT("الدردشة الإدراكية", Icons.Default.SmartToy),
    DOMAINS("المحركات (23)", Icons.Default.Layers),
    CINEMA("استوديو السينما", Icons.Default.Movie),
    SYNC("السيادة والمزامنة", Icons.Default.Shield)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SasaHomeScreen(
    viewModel: SasaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(NeamaNavTab.CHAT) }
    var inputText by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    var showGlobalPreview by remember { mutableStateOf(false) }

    // Find latest HTML or code block content across all messages for default preview
    val defaultHtml = "<!DOCTYPE html>\n<html lang=\"ar\" dir=\"rtl\">\n<head>\n  <meta charset=\"UTF-8\">\n  <title>معاينة صاصا AI</title>\n  <style>\n    body { font-family: sans-serif; background: #0f172a; color: #f8fafc; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }\n    .card { background: #1e293b; padding: 2rem; border-radius: 1rem; border: 1px solid #38bdf8; text-align: center; }\n  </style>\n</head>\n<body>\n  <div class=\"card\">\n    <h2>🚀 شاشة العرض والمشاهدة المباشرة (Sasa Live Preview)</h2>\n    <p>جاهزة لتشغيل وعرض واجهات الـ HTML، الـ CSS، الـ Web، وجميع التصاميم البرمجية فورياً!</p>\n  </div>\n</body>\n</html>"

    val latestCodeOrHtml = remember(uiState.messages) {
        val lastMessage = uiState.messages.lastOrNull { it.sender == com.example.data.MessageSender.SASA_AI }
        if (lastMessage != null) {
            val regex = Regex("```(?:html|htm|web|xml)?\\n([\\s\\S]*?)```")
            val match = regex.find(lastMessage.text)
            match?.groupValues?.get(1)?.trim() ?: defaultHtml
        } else {
            defaultHtml
        }
    }

    if (showGlobalPreview) {
        PreviewDialog(
            title = "شاشة العرض والمعاينة المباشرة (Live Viewer)",
            content = latestCodeOrHtml,
            language = "html",
            onDismiss = { showGlobalPreview = false }
        )
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Feedback state map: messageId -> Boolean (true = Up, false = Down)
    val feedbackState = remember { mutableStateMapOf<String, Boolean>() }

    // Text-To-Speech setup
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale.forLanguageTag("ar")
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Voice input launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
            }
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "file"
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    val fileContext = "\n\n--- 📁 محتوى الملف المرفق تلقائياً ($fileName) ---\n${content.take(40000)}\n--------------------------------------------------"
                    inputText = if (inputText.isBlank()) "قم بتحليل ومعالجة هذا الملف المرفق:\n$fileContext" else "$inputText\n$fileContext"
                    Toast.makeText(context, "تم إرفاق الملف وقراءة محتواه بنجاح: $fileName", Toast.LENGTH_SHORT).show()
                } else {
                    val noticeText = "📁 تم إرفاق الملف: $fileName"
                    inputText = if (inputText.isBlank()) noticeText else "$inputText\n$noticeText"
                    Toast.makeText(context, "تم إرفاق الملف بنجاح: $fileName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val noticeText = "📁 تم إرفاق الملف: $fileName"
                inputText = if (inputText.isBlank()) noticeText else "$inputText\n$noticeText"
                Toast.makeText(context, "تم إرفاق الملف: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isGenerating) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show system notice if any
    LaunchedEffect(uiState.systemNotice) {
        uiState.systemNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.dismissSystemNotice()
        }
    }

    // Force RTL for Arabic layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                HeaderBar(
                    selectedModel = uiState.selectedModel,
                    isWebSearchEnabled = uiState.isWebSearchEnabled,
                    onModelClick = { showModelMenu = true },
                    onPreviewClick = { showGlobalPreview = true },
                    onMemoryClick = { viewModel.setShowMemoryDialog(true) },
                    onWebSearchToggle = { viewModel.toggleWebSearch() },
                    onVoiceCallClick = { viewModel.setShowVoiceCallDialog(true) },
                    onCloudSettingsClick = { viewModel.setShowCloudWorkspaceSettings(true) },
                    onGitHubClick = { viewModel.setShowGitHubDialog(true) },
                    onCreativeStudioClick = { viewModel.setShowCreativeStudioDialog(true) },
                    onNeamaHubClick = { viewModel.setShowNeamaHubDialog(true) },
                    onClearChatClick = { viewModel.onClearChat() },
                    onPlansClick = { viewModel.setShowPlansDialog(true) },
                    onProfileClick = { viewModel.setShowProfileDialog(true) },
                    onNewSessionClick = { viewModel.onNewSession() }
                )
            },
            bottomBar = {
                Column {
                    if (currentTab == NeamaNavTab.CHAT) {
                        BottomInputBar(
                            inputText = inputText,
                            onInputChanged = { inputText = it },
                            isGenerating = uiState.isGenerating,
                            activeModelName = uiState.selectedModel.displayName,
                            onSend = {
                                if (inputText.isNotBlank() && !uiState.isGenerating) {
                                    val textToSend = inputText.trim()
                                    inputText = ""
                                    viewModel.onSendMessage(textToSend)
                                }
                            },
                            onStopGeneration = {
                                viewModel.stopGeneration()
                            },
                            onAttachFile = {
                                filePickerLauncher.launch("*/*")
                            },
                            onVoiceInput = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن للاستماع لصلبك البرمجي...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "الميزة غير متوفرة على هذا الجهاز", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onVoiceCallClick = { viewModel.setShowVoiceCallDialog(true) }
                        )
                    }

                    // Unified Sovereign Navigation Bar
                    NavigationBar(
                        containerColor = Color(0xFF0F172A),
                        tonalElevation = 8.dp
                    ) {
                        NeamaNavTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B)
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0xFF1E293B)
                                )
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = SasaDarkBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Long-Term Memory Dialog
                if (uiState.showMemoryDialog) {
                    MemoryDialog(
                        memories = uiState.projectMemories,
                        onDismiss = { viewModel.setShowMemoryDialog(false) },
                        onAddMemory = { key, content -> viewModel.addProjectMemory(key, content) },
                        onDeleteMemory = { key -> viewModel.deleteProjectMemory(key) },
                        onClearAll = { viewModel.clearProjectMemories() }
                    )
                }

                // Cloud Workspace Settings Dialog
                if (uiState.showCloudWorkspaceSettings) {
                    CloudWorkspaceSettingsDialog(
                        config = uiState.cloudWorkspaceConfig,
                        onSaveConfig = { viewModel.updateCloudWorkspaceConfig(it) },
                        onTestConnection = { viewModel.testCloudWorkspaceExecution(it) },
                        onDismiss = { viewModel.setShowCloudWorkspaceSettings(false) }
                    )
                }

                // GitHub Manager Dialog
                if (uiState.showGitHubDialog) {
                    GitHubManagerDialog(
                        token = uiState.githubToken,
                        userStatus = uiState.githubUserStatus,
                        repos = uiState.githubRepos,
                        selectedRepo = uiState.selectedRepo,
                        repoTree = uiState.repoTree,
                        selectedFile = uiState.selectedFile,
                        isLoading = uiState.isLoadingGitHub,
                        onTokenSave = { viewModel.setGitHubToken(it) },
                        onSelectRepo = { viewModel.setSelectedRepo(it) },
                        onOpenFile = { owner, repo, path, branch -> viewModel.openRepoFile(owner, repo, path, branch) },
                        onCommitFile = { owner, repo, path, content, msg, sha, branch -> viewModel.commitFileChanges(owner, repo, path, content, msg, sha, branch) },
                        onForkRepo = { owner, repo -> viewModel.forkRepo(owner, repo) },
                        onDismiss = { viewModel.setShowGitHubDialog(false) }
                    )
                }

                // Voice Call & Live Screen Share Dialog
                if (uiState.showVoiceCallDialog) {
                    VoiceCallDialog(
                        isOpen = uiState.showVoiceCallDialog,
                        onDismiss = { viewModel.setShowVoiceCallDialog(false) },
                        activeWorkspaceSummary = "مشروع صاصا AI (صاصا v15.5) - متصل ومجهز بتراسل الخدمات الشفافة وبث شاشة المعاينة الحية",
                        onExecutePrompt = { prompt -> viewModel.onSendMessage(prompt) },
                        isProcessingMessage = uiState.isGenerating
                    )
                }

                // Creative Studio & Cinema Directing Dialog
                if (uiState.showCreativeStudioDialog) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { viewModel.setShowCreativeStudioDialog(false) },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        CreativeStudioScreen(
                            onDismiss = { viewModel.setShowCreativeStudioDialog(false) }
                        )
                    }
                }

                // Neama Sovereign Cognitive Hub Dialog (23 Unified Domains)
                if (uiState.showNeamaHubDialog) {
                    NeamaDomainsHubDialog(
                        isOpen = uiState.showNeamaHubDialog,
                        onDismiss = { viewModel.setShowNeamaHubDialog(false) },
                        onInjectDomainPrompt = { prompt -> viewModel.onSendMessage(prompt) }
                    )
                }

                // Plans Dialog
                if (uiState.showPlansDialog) {
                    PlansDialog(
                        isOpen = uiState.showPlansDialog,
                        onDismiss = { viewModel.setShowPlansDialog(false) }
                    )
                }

                // Profile & PAT Dialog
                if (uiState.showProfileDialog) {
                    ProfileDialog(
                        isOpen = uiState.showProfileDialog,
                        onDismiss = { viewModel.setShowProfileDialog(false) }
                    )
                }

                when (currentTab) {
                    NeamaNavTab.CHAT -> {
                        Column(modifier = Modifier.fillMaxSize()) {

                            // Chat messages list
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }

                                items(uiState.messages, key = { it.id }) { msg ->
                                    ChatMessageItem(
                                        message = msg,
                                        feedbackValue = feedbackState[msg.id],
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(msg.text))
                                            Toast.makeText(context, "تم نسخ النص إلى الحافظة", Toast.LENGTH_SHORT).show()
                                        },
                                        onListen = {
                                            ttsEngine?.stop()
                                            ttsEngine?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, msg.id)
                                        },
                                        onShare = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, msg.text)
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "مشاركة رد نعمة AI")
                                            context.startActivity(shareIntent)
                                        },
                                        onPushToCloud = { path, content ->
                                            viewModel.pushUpdateToCloudRepo(path, content)
                                        },
                                        onFeedback = { isUp ->
                                            if (feedbackState[msg.id] == isUp) {
                                                feedbackState.remove(msg.id)
                                            } else {
                                                feedbackState[msg.id] = isUp
                                                val feedbackMsg = if (isUp) "شكراً لك على التقييم الإيجابي! 👍" else "شكراً لملاحظاتك، سنعمل على تحسين الإجابات. 👎"
                                                Toast.makeText(context, feedbackMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }

                                if (uiState.isGenerating) {
                                    item {
                                        ThinkingIndicator(modelName = uiState.selectedModel.displayName)
                                    }
                                }

                                // Clean empty-state greeting when conversation is empty
                                if (uiState.messages.isEmpty() && !uiState.isGenerating) {
                                    item {
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(SasaPrimaryContainer)
                                                    .border(2.dp, SasaPrimary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "نعمة AI",
                                                    tint = SasaSecondary,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "مرحباً بك في منظومة نعمة الذكية (Neama AI)",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "المنظومة المعرفية السيادية الموحدة للذكاء الاصطناعي والهندسة البرمجية",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SasaAccentGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "23 محركاً تخصصياً، الاستوديو السينمائي، ومزامنة GitHub اللحظية جاهزة كلياً.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SasaTextSecondary,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))

                                            @OptIn(ExperimentalLayoutApi::class)
                                            FlowRow(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                PromptChip(label = "📞 اتصال صوتي حي مع نعمة AI") {
                                                    viewModel.setShowVoiceCallDialog(true)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                PromptChip(label = "🤖 ما هي إمكانيات منظومة نعمة؟") {
                                                    viewModel.onSendMessage("من أنت وما هي جميع الخدمات والإمكانيات التي تقدمها منظومة نعمة بالتفصيل؟")
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                PromptChip(label = "🏛️ استعراض المحركات (23)") {
                                                    currentTab = NeamaNavTab.DOMAINS
                                                }
                                            }
                                        }
                                    }
                                }

                                item { Spacer(modifier = Modifier.height(12.dp)) }
                            }
                        }
                    }
                    NeamaNavTab.DOMAINS -> {
                        NeamaDomainsScreen(
                            onOpenCinemaStudio = { currentTab = NeamaNavTab.CINEMA }
                        )
                    }
                    NeamaNavTab.CINEMA -> {
                        CreativeStudioScreen(
                            onDismiss = { currentTab = NeamaNavTab.CHAT }
                        )
                    }
                    NeamaNavTab.SYNC -> {
                        NeamaSovereignSyncScreen(
                            onOpenGitHubManager = { viewModel.setShowGitHubDialog(true) },
                            onOpenCloudWorkspaceSettings = { viewModel.setShowCloudWorkspaceSettings(true) },
                            onOpenMemoryManager = { viewModel.setShowMemoryDialog(true) }
                        )
                    }
                }

                // Dropdown menu for model selection
                DropdownMenu(
                    expanded = showModelMenu,
                    onDismissRequest = { showModelMenu = false },
                    modifier = Modifier.background(SasaCardBackground)
                ) {
                    GeminiModel.entries.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (model == uiState.selectedModel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (model == uiState.selectedModel) SasaPrimary else Color.Unspecified
                                    )
                                    Text(
                                        text = model.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SasaTextSecondary
                                    )
                                }
                            },
                            onClick = {
                                viewModel.onSelectModel(model)
                                showModelMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderBar(
    selectedModel: GeminiModel = GeminiModel.FLASH_3_6,
    isWebSearchEnabled: Boolean = true,
    onModelClick: () -> Unit = {},
    onPreviewClick: () -> Unit = {},
    onMemoryClick: () -> Unit = {},
    onWebSearchToggle: () -> Unit = {},
    onVoiceCallClick: () -> Unit = {},
    onCloudSettingsClick: () -> Unit = {},
    onGitHubClick: () -> Unit = {},
    onCreativeStudioClick: () -> Unit = {},
    onNeamaHubClick: () -> Unit = {},
    onClearChatClick: () -> Unit = {},
    onPlansClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNewSessionClick: () -> Unit = {}
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf("العربية") }

    Surface(
        color = SasaCardBackground,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title & Transparent Background Service Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SasaPrimaryContainer)
                        .border(1.5.dp, SasaPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "نعمة AI",
                        tint = SasaSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "نعمة AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SasaAccentGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SasaAccentGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "23 محركاً سيادياً موحداً 🟢",
                                    fontSize = 10.sp,
                                    color = SasaAccentGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Top Primary Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Neama Hub Direct Button
                Surface(
                    onClick = onNeamaHubClick,
                    shape = RoundedCornerShape(10.dp),
                    color = SasaPrimary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SasaPrimary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "محركات نعمة الـ 23",
                            tint = SasaSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "المحركات",
                            color = SasaSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // New Session Button (جلسة جديدة)
                Surface(
                    onClick = onNewSessionClick,
                    shape = RoundedCornerShape(10.dp),
                    color = SasaPrimary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SasaPrimary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "جلسة جديدة",
                            tint = SasaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "جلسة جديدة",
                            color = SasaPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Voice Call & Live Screen Share Button (زر الاتصال)
                Surface(
                    onClick = onVoiceCallClick,
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1B5E20).copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SasaAccentGreen)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "زر الاتصال والمكالمة الصوتية الحية",
                            tint = SasaAccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "اتصال",
                            color = SasaAccentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Preview Screen Button
                IconButton(
                    onClick = onPreviewClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SasaPrimary.copy(alpha = 0.15f))
                        .border(1.dp, SasaPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "شاشة العرض المعاينة",
                        tint = SasaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // More Menu for Transparent Services & Tools
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SasaPrimary.copy(alpha = 0.15f))
                            .border(1.dp, SasaPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "قائمة الخدمات والأنظمة الخلفية",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier
                            .background(SasaCardBackground)
                            .border(1.dp, SasaPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("👑 الخطط والاشتراكات (Plans)", color = Color(0xFFF59E0B), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMoreMenu = false
                                onPlansClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📁 المشاريع ومستودعات GitHub", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onGitHubClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("👤 الملف الشخصي وتوليد التوكن (PAT)", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onProfileClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚙️ الإعدادات والذاكرة طويلة المدى", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onMemoryClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "🌐 اللغة: $currentLanguage (تبديل)", 
                                    color = SasaSecondary, 
                                    fontSize = 13.sp
                                ) 
                            },
                            onClick = {
                                currentLanguage = if (currentLanguage == "العربية") "English" else "العربية"
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("✨ بدء جلسة جديدة وتصفير السياق", color = SasaPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showMoreMenu = false
                                onNewSessionClick()
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        DropdownMenuItem(
                            text = { Text("🤖 تغيير نموذج الذكاء الاصطناعي", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onModelClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isWebSearchEnabled) "🌐 البحث المباشر في الويب (مفعل خلفياً 🟢)" else "🌐 البحث المباشر في الويب (معطل 🔴)",
                                    color = if (isWebSearchEnabled) SasaAccentGreen else Color.White,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onWebSearchToggle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("☁️ إعدادات وتراسل البيئة السحابية (Codespaces)", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onCloudSettingsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🌌 كتالوج محركات نعمة الـ 23 (Neama Hub)", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onNeamaHubClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🎬 الاستوديو الإبداعي والإخراج السينمائي (Neama)", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onCreativeStudioClick()
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        DropdownMenuItem(
                            text = { Text("🗑️ مسح محادثة الجلسة الحالية", color = MaterialTheme.colorScheme.error, fontSize = 13.sp) },
                            onClick = {
                                showMoreMenu = false
                                onClearChatClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    feedbackValue: Boolean? = null,
    onCopy: () -> Unit = {},
    onListen: () -> Unit = {},
    onShare: () -> Unit = {},
    onTranslate: () -> Unit = {},
    onPushToCloud: ((filePath: String, content: String) -> Unit)? = null,
    onFeedback: (Boolean) -> Unit = {}
) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM

    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFF3E2723),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6D00))
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFD180),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SasaPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "صاصا",
                    tint = SasaSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 4.dp else 16.dp,
                bottomEnd = if (isUser) 16.dp else 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) SasaUserBubble else if (message.isError) Color(0xFF3C1818) else SasaAiBubble
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .testTag(if (isUser) "user_message_bubble" else "ai_message_bubble")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صاصا AI",
                            style = MaterialTheme.typography.labelMedium,
                            color = SasaSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        message.modelUsed?.let { model ->
                            Text(
                                text = model,
                                style = MaterialTheme.typography.labelSmall,
                                color = SasaTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                MessageTextWithCodeBlocks(
                    text = message.text,
                    onPushToCloud = onPushToCloud
                )

                // Render Media Attachment if present (Generated Image, Video, Movie, SVG)
                val directMediaUrl = message.mediaUrl ?: run {
                    val regex = Regex("""\[(https?://[^\s)]+|/api/media/[^\s)]+|data:image/[^\s)]+)\]\(\1\)""")
                    val m = regex.find(message.text)
                    m?.groupValues?.get(1)
                }

                if (!directMediaUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MediaAttachmentCard(
                        mediaUrl = directMediaUrl,
                        mediaType = message.mediaType ?: "وسائط",
                        mediaTitle = message.mediaTitle ?: "معاينة ملف الوسائط المنجز"
                    )
                }

                // Action buttons under AI responses (Copy, Listen, Share, Thumb Up, Thumb Down)
                if (!isUser && !message.isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copy Button
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("copy_response_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ الرد",
                                tint = SasaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Listen/TTS Button
                        IconButton(
                            onClick = onListen,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("listen_response_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "استماع للرد",
                                tint = SasaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("share_response_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة الرد",
                                tint = SasaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Translate Button
                        IconButton(
                            onClick = onTranslate,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("translate_response_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "ترجمة الرد",
                                tint = SasaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Rating Thumb Up
                        IconButton(
                            onClick = { onFeedback(true) },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("thumb_up_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "إعجاب بالرد",
                                tint = if (feedbackValue == true) SasaPrimary else SasaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Rating Thumb Down
                        IconButton(
                            onClick = { onFeedback(false) },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("thumb_down_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "لم يعجبني",
                                tint = if (feedbackValue == false) Color.Red else SasaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator(modelName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = SasaSecondary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "جارٍ التفكير ومعالجة الرد عبر $modelName...",
            style = MaterialTheme.typography.bodySmall,
            color = SasaTextSecondary
        )
    }
}

@Composable
fun PromptChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = SasaCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, SasaPrimary.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun BottomInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    isGenerating: Boolean,
    activeModelName: String,
    onSend: () -> Unit,
    onStopGeneration: () -> Unit = {},
    onAttachFile: () -> Unit = {},
    onVoiceInput: () -> Unit = {},
    onVoiceCallClick: () -> Unit = {}
) {
    Surface(
        color = SasaDarkSurface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // File Attachment Button
            IconButton(
                onClick = onAttachFile,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("attach_file_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "رفع ملف",
                    tint = SasaPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Voice Input Button
            IconButton(
                onClick = onVoiceInput,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("voice_input_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "إدخال صوتي",
                    tint = SasaSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Main Message Input Text Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = "اكتب استفسارك لـ صاصا AI...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SasaTextSecondary
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Send
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSend = {
                        if (inputText.trim().isNotEmpty() && !isGenerating) {
                            onSend()
                        }
                    }
                ),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SasaCardBackground,
                    unfocusedContainerColor = SasaCardBackground,
                    focusedBorderColor = SasaPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Live Voice Call Button right next to Send button
            Surface(
                onClick = onVoiceCallClick,
                shape = CircleShape,
                color = Color(0xFF10B981).copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("live_chat_call_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "بدء محادثة صوتية مباشرة",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // High-Visibility Standalone Send / Stop Button
            val canSend = inputText.trim().isNotEmpty() && !isGenerating

            Surface(
                onClick = {
                    if (isGenerating) {
                        onStopGeneration()
                    } else if (canSend) {
                        onSend()
                    }
                },
                enabled = canSend || isGenerating,
                shape = CircleShape,
                color = when {
                    isGenerating -> Color(0xFFEF4444) // Sovereign Red stop button
                    canSend -> SasaPrimary
                    else -> SasaCardBackground
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_message_button"),
                shadowElevation = if (canSend || isGenerating) 4.dp else 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isGenerating) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إيقاف التوليد",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = if (canSend) Color.Black else SasaTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MediaAttachmentCard(
    mediaUrl: String,
    mediaType: String,
    mediaTitle: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isVideo = mediaType.contains("video", ignoreCase = true) ||
            mediaType.contains("movie", ignoreCase = true) ||
            mediaType.contains("cinema", ignoreCase = true) ||
            mediaType.contains("film", ignoreCase = true) ||
            mediaType.contains("مسلسل", ignoreCase = true) ||
            mediaType.contains("فيلم", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, SasaPrimary.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("media_attachment_card")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF020617))
                    .clickable {
                        try {
                            if (mediaUrl.startsWith("http")) {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(mediaUrl))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "معاينة: $mediaTitle", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "الملف جاهز: $mediaTitle", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = mediaTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل العرض",
                            tint = SasaPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${mediaType.uppercase()} • جاهز للمعاينة الفورية والتحميل",
                        style = MaterialTheme.typography.labelSmall,
                        color = SasaPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = {
                        try {
                            if (mediaUrl.startsWith("http")) {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(mediaUrl))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "تم تجهيز رابط التحميل بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "الرابط جاهز للتحميل", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = SasaPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "تحميل",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحميل", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
