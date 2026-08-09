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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SasaHomeScreen(
    viewModel: SasaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
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
                ttsEngine?.language = Locale("ar")
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
                HeaderBar()
            },
            bottomBar = {
                BottomInputBar(
                    inputText = inputText,
                    onInputChanged = { inputText = it },
                    isGenerating = uiState.isGenerating,
                    activeModelName = uiState.selectedModel.displayName,
                    onSend = {
                        if (inputText.isNotBlank() && !uiState.isGenerating) {
                            val textToSend = inputText
                            inputText = ""
                            viewModel.onSendMessage(textToSend)
                        }
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
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = SasaDarkBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
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
                                    val shareIntent = Intent.createChooser(sendIntent, "مشاركة رد صاصا AI")
                                    context.startActivity(shareIntent)
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

                        // Quick prompt suggestion chips if conversation is brief
                        if (uiState.messages.size <= 2 && !uiState.isGenerating) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "💡 اقتراحات سريعة للبدء:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SasaTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PromptChip("🔗 تحليل رابط مستودع GitHub") {
                                        inputText = "قم بفحص وتحليل مستودع GitHub التالي واشرح بنيته وأبرز ملفاته: https://github.com/"
                                    }
                                    PromptChip("🎬 سيناريو فيلم سينمائي") {
                                        inputText = "اكتب لي سيناريو فيلم سينمائي تشويقي متكامل الحوار والمشاهد مع وصف اللقطات والإخراج."
                                    }
                                    PromptChip("🎥 وصف فيديو AI (Sora/Runway)") {
                                        inputText = "صمم لي برومبت دقيق وعالي الجودة لتوليد مشهد فيديو ذكاء اصطناعي سينمائي بصيغة 4K."
                                    }
                                    PromptChip("💻 كتابة كود Python أو Kotlin") {
                                        inputText = "اكتب لي كوداً محترفاً ومكتتملاً لنظام إدارة مهام مع شرح العمليات."
                                    }
                                    PromptChip("⚡ تحليل ومعالجة خطأ برمجي") {
                                        inputText = "كيف أعالج خطأ Quota limits exceeded (429) في استخدام Gemini API برمجياً مع التغيير التلقائي للنماذج؟"
                                    }
                                    PromptChip("📝 تلخيص وتبسيط الفكرة") {
                                        inputText = "اشرح لي مفهوم التفكير المعماري للذكاء الاصطناعي وكيف يختار النماذج المناسبة."
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
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
fun HeaderBar() {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SasaPrimaryContainer)
                        .border(1.5.dp, SasaPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "صاصا AI",
                        tint = SasaSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "منظومة صاصا AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SasaAccentGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v15.2 Pro",
                                fontSize = 10.sp,
                                color = SasaAccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "بيئة وكيل البرمجة والخدمات الخلفية الذكية ⚡",
                        style = MaterialTheme.typography.labelSmall,
                        color = SasaTextSecondary
                    )
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

                MessageTextWithCodeBlocks(text = message.text)

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
                                imageVector = Icons.Default.VolumeUp,
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
    onAttachFile: () -> Unit = {},
    onVoiceInput: () -> Unit = {}
) {
    Surface(
        color = SasaDarkSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = "اكتب استفسارك أو طلبك البرمجي لـ صاصا AI...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SasaTextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        // File attachment button
                        IconButton(
                            onClick = onAttachFile,
                            modifier = Modifier.testTag("attach_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "رفع ملف",
                                tint = SasaPrimary
                            )
                        }

                        // Voice input button
                        IconButton(
                            onClick = onVoiceInput,
                            modifier = Modifier.testTag("voice_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "إدخال صوتي",
                                tint = SasaSecondary
                            )
                        }
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputText.trim().isNotEmpty() && !isGenerating) {
                                onSend()
                            }
                        },
                        enabled = inputText.trim().isNotEmpty() && !isGenerating,
                        modifier = Modifier.testTag("send_message_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SasaPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (inputText.trim().isNotEmpty() && !isGenerating) SasaPrimary else SasaTextSecondary.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
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
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SasaCardBackground,
                    unfocusedContainerColor = SasaCardBackground,
                    focusedBorderColor = SasaPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}
