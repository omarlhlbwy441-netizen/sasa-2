package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SasaAccentGreen
import com.example.ui.theme.SasaCardBackground
import com.example.ui.theme.SasaDarkSurface
import com.example.ui.theme.SasaPrimary
import com.example.ui.theme.SasaSecondary
import com.example.ui.theme.SasaTextSecondary
import java.util.UUID

@Composable
fun ProfileDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return
    val context = LocalContext.current
    var sovereignToken by remember { mutableStateOf("neama_pat_live_" + UUID.randomUUID().toString().replace("-", "").take(20)) }
    var copyNotice by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = SasaDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SasaPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👤 الملف الشخصي وتوليد التوكن",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SasaCardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SasaPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SasaPrimary.copy(alpha = 0.2f))
                                    .border(2.dp, SasaPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ع",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = SasaPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = "عمر الصادق محمد أحمد إدريس",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "المطور ومصمم البرمجيات الأساسي 🛡️",
                                    fontSize = 12.sp,
                                    color = SasaAccentGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Token Generation Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SasaCardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = SasaSecondary)
                                Text(
                                    text = "رمز الوصول والتوكن السيادي (Neama PAT):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }

                            OutlinedTextField(
                                value = sovereignToken,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Sovereign Token", sovereignToken))
                                            copyNotice = true
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ التوكن", tint = SasaPrimary)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SasaDarkSurface,
                                    unfocusedContainerColor = SasaDarkSurface,
                                    focusedTextColor = SasaPrimary,
                                    unfocusedTextColor = SasaPrimary
                                )
                            )

                            if (copyNotice) {
                                Text(
                                    text = "تم نسخ التوكن بنجاح إلى الحافظة 📋",
                                    color = SasaAccentGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    sovereignToken = "neama_pat_live_" + UUID.randomUUID().toString().replace("-", "").take(20)
                                    copyNotice = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SasaPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("⚡ توليد توكن جديد للمنظومة", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "الصلاحيات الممنوحة لهذا التوكن:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text(text = "• ✅ صلاحيات قراءة وفحص ومزامنة مستودعات GitHub", fontSize = 12.sp, color = SasaTextSecondary)
                            Text(text = "• ✅ تشغيل الأوامر والأكواد عبر Open Interpreter", fontSize = 12.sp, color = SasaTextSecondary)
                            Text(text = "• ✅ الوصول لمحرك الوسائط والأفلام والذاكرة الدائمة", fontSize = 12.sp, color = SasaTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
