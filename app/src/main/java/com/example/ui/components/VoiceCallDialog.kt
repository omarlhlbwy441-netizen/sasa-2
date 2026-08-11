package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale

// Modern Dark Theme Colors matching Sasa AI
private val SasaDarkCanvas = Color(0xFF0F172A)
private val SasaCardBg = Color(0xFF1E293B)
private val SasaPrimaryCyan = Color(0xFF06B6D4)
private val SasaAccentGreen = Color(0xFF10B981)
private val SasaPurple = Color(0xFF8B5CF6)
private val SasaRose = Color(0xFFF43F5E)
private val SasaGold = Color(0xFFF59E0B)
private val SasaTextPrimary = Color(0xFFF8FAFC)
private val SasaTextSecondary = Color(0xFF94A3B8)

@Composable
fun VoiceCallDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    activeWorkspaceSummary: String?,
    onExecutePrompt: (String) -> Unit,
    isProcessingMessage: Boolean
) {
    if (!isOpen) return

    val context = LocalContext.current
    var isMicMuted by remember { mutableStateOf(false) }
    var isSpeakerMuted by remember { mutableStateOf(false) }
    var isScreenSharing by remember { mutableStateOf(true) }
    var callStatusText by remember { mutableStateOf("متصل ميكروفون وبث مباشر 🟢") }
    var speechInputText by remember { mutableStateOf("") }
    var currentAiSpeechText by remember { mutableStateOf("أهلاً بك! أنا 'صاصا AI' جاهز للتحدث معك فورياً ومشاركة الشاشة. تحدث معي أو اطلب تصوراً وسأنفذه لك صوتاً وكوداً!") }
    
    // Text To Speech Engine setup
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let {
                    it.language = Locale("ar")
                    it.setSpeechRate(0.95f)
                    ttsEngine = it
                    // Initial greeting speech
                    if (!isSpeakerMuted) {
                        isSpeaking = true
                        it.speak(
                            "مرحباً بك في جلسة المكالمة المباشرة وتطوير المشاريع مع صاصا أي أي. تحدث معي أو اطلب تصوراً صوتاً وسأنفذه لك فوراً",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "greeting_id"
                        )
                    }
                }
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Function to speak AI response verbally
    fun speakText(text: String) {
        if (isSpeakerMuted) return
        ttsEngine?.let { tts ->
            tts.stop()
            isSpeaking = true
            val cleanSpeech = text.replace(Regex("""```[\s\S]*?```"""), " تم بناء الكود البرمجي المرفق بنجاح. ")
                .replace(Regex("""[*#_~`-]"""), " ")
            tts.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "speech_${System.currentTimeMillis()}")
        }
    }

    // Pulse Animation for Voice Waves
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = {
            ttsEngine?.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SasaDarkCanvas)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(SasaAccentGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "مكالمة صوتية وتطوير حي (Sasa Live Call)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SasaTextPrimary
                            )
                            Text(
                                text = callStatusText,
                                fontSize = 12.sp,
                                color = SasaAccentGreen
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { isScreenSharing = !isScreenSharing },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isScreenSharing) SasaPrimaryCyan.copy(alpha = 0.2f) else SasaCardBg)
                        ) {
                            Icon(
                                imageVector = if (isScreenSharing) Icons.Default.ScreenShare else Icons.Default.StopScreenShare,
                                contentDescription = "مشاركة الشاشة",
                                tint = if (isScreenSharing) SasaPrimaryCyan else SasaTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                ttsEngine?.stop()
                                onDismiss()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SasaRose)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "إنهاء المكالمة",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Middle Content Area: Live Screen Share Frame & Audio Avatar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SasaCardBg)
                        .border(1.dp, SasaPrimaryCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Screen Share Status & Live Workspace Banner
                        if (isScreenSharing) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SasaDarkCanvas.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, SasaPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cast,
                                        contentDescription = null,
                                        tint = SasaPurple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📺 بث مشاركة الشاشة وبناء الأكواد الحية",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SasaTextPrimary
                                        )
                                        Text(
                                            text = activeWorkspaceSummary ?: "بيئة العمل جاهزة ومتصلة بفرع GitHub ومحرر الأكواد الصوتي.",
                                            fontSize = 11.sp,
                                            color = SasaTextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Animated Sasa AI Avatar & Audio Visualizer
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            // Pulsing Outer Waves
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .scale(if (isProcessingMessage || isSpeaking) pulseScale else 1f)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                SasaPrimaryCyan.copy(alpha = 0.4f),
                                                SasaPurple.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(SasaPrimaryCyan, SasaPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "صوت صاصا AI",
                                    tint = Color.White,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Speech Text / AI Response Box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SasaDarkCanvas),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SasaPrimaryCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = SasaPrimaryCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isProcessingMessage) "جارٍ التفكير والتنفيذ البرمجي... ⚡" else "رد صاصا الصوتي 🎙️:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SasaPrimaryCyan
                                    )
                                }
                                Text(
                                    text = currentAiSpeechText,
                                    fontSize = 13.sp,
                                    color = SasaTextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Verbal Commands Chips ("اعطيني تصور", "لننفذ هذا الآن", "طور هذا المشروع")
                        Text(
                            text = "💡 اختصارات صوتية سريعة أثناء المكالمة:",
                            fontSize = 12.sp,
                            color = SasaTextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            textAlign = TextAlign.Start
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {
                                    val prompt = "اعطيني تصوراً برمجياً كاملاً وهندسة معمارية للمشروع المفتوح صوتاً وكوداً"
                                    speechInputText = prompt
                                    currentAiSpeechText = "جارٍ إعداد التصور الهندسي الشامل صوتاً وكوداً..."
                                    speakText("جارٍ تحليل بيئة العمل وإعداد تصور هندسي متكامل للمشروع")
                                    onExecutePrompt(prompt)
                                },
                                label = { Text("💡 اعطيني تصوراً", fontSize = 11.sp, color = SasaPrimaryCyan) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = SasaDarkCanvas)
                            )
                            AssistChip(
                                onClick = {
                                    val prompt = "لننفذ هذا التصور الهندسي الآن! ابدأ في إنشاء الملفات وكتابة الأكواد الكاملة وربطها مع GitHub"
                                    speechInputText = prompt
                                    currentAiSpeechText = "ممتاز! بدأنا التنفيذ البرمجي التلقائي ورفع التحديثات لـ GitHub..."
                                    speakText("ممتاز جداً! نبدأ الآن في تنفيذ الأكواد وبناء الملفات آلياً ورفعها لمستودع جيتهاب")
                                    onExecutePrompt(prompt)
                                },
                                label = { Text("🚀 لننفذ هذا الآن!", fontSize = 11.sp, color = SasaAccentGreen) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = SasaDarkCanvas)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Call Control Bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = SasaCardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Input TextField for typing or quick speech preview
                        OutlinedTextField(
                            value = speechInputText,
                            onValueChange = { speechInputText = it },
                            placeholder = { Text("اكتب أو تحدث هنا أثناء المكالمة الصوتية...", fontSize = 12.sp, color = SasaTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (speechInputText.isNotBlank()) {
                                            val prompt = speechInputText
                                            currentAiSpeechText = "استلمت: $prompt - جارٍ المعالجة الصوتية والتنفيذ..."
                                            speakText("جارٍ تنفيذ الطلب $prompt")
                                            onExecutePrompt(prompt)
                                            speechInputText = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "إرسال",
                                        tint = SasaPrimaryCyan
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SasaPrimaryCyan,
                                unfocusedBorderColor = SasaTextSecondary.copy(alpha = 0.3f),
                                focusedTextColor = SasaTextPrimary,
                                unfocusedTextColor = SasaTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mic Mute Toggle
                            IconButton(
                                onClick = { isMicMuted = !isMicMuted },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isMicMuted) SasaRose.copy(alpha = 0.2f) else SasaDarkCanvas)
                                    .border(1.dp, if (isMicMuted) SasaRose else SasaPrimaryCyan, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "كتم الميكروفون",
                                    tint = if (isMicMuted) SasaRose else SasaPrimaryCyan
                                )
                            }

                            // Speaker Mute Toggle
                            IconButton(
                                onClick = {
                                    isSpeakerMuted = !isSpeakerMuted
                                    if (isSpeakerMuted) ttsEngine?.stop()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isSpeakerMuted) SasaRose.copy(alpha = 0.2f) else SasaDarkCanvas)
                                    .border(1.dp, if (isSpeakerMuted) SasaRose else SasaAccentGreen, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isSpeakerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "كتم الصوت",
                                    tint = if (isSpeakerMuted) SasaRose else SasaAccentGreen
                                )
                            }

                            // End Call Button
                            Button(
                                onClick = {
                                    ttsEngine?.stop()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SasaRose),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إنهاء المكالمة", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
