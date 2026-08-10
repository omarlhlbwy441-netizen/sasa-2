package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BackgroundServiceManager
import com.example.data.CloudWorkspaceConfig
import com.example.ui.theme.SasaDarkSurface
import com.example.ui.theme.SasaPrimary
import com.example.ui.theme.SasaTextPrimary
import com.example.ui.theme.SasaTextSecondary

@Composable
fun CloudWorkspaceSettingsDialog(
    config: CloudWorkspaceConfig,
    onSaveConfig: (CloudWorkspaceConfig) -> Unit,
    onTestConnection: (CloudWorkspaceConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var workspaceUrl by remember { mutableStateOf(config.workspaceUrl) }
    var token by remember { mutableStateOf(config.token) }
    var envName by remember { mutableStateOf(config.environmentName) }
    var isAutoSync by remember { mutableStateOf(config.isAutoSyncEnabled) }
    var isBgServiceActive by remember { mutableStateOf(config.isBackgroundServiceActive) }

    val backgroundTasks by BackgroundServiceManager.tasks.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .clip(RoundedCornerShape(18.dp)),
            color = SasaDarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SasaPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = SasaPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "إعدادات البيئة السحابية (Codespaces)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SasaTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ربط التطبيق ببيئة العمل السحابية الخارجية لـ Codespaces وتفعيل تنفيذ السكربتات والأنظمة خلفياً بأسلوب شفاف.",
                    fontSize = 13.sp,
                    color = SasaTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Inputs
                OutlinedTextField(
                    value = envName,
                    onValueChange = { envName = it },
                    label = { Text("اسم البيئة السحابية") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SasaPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = SasaPrimary,
                        focusedTextColor = SasaTextPrimary,
                        unfocusedTextColor = SasaTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = workspaceUrl,
                    onValueChange = { workspaceUrl = it },
                    label = { Text("رابط بيئة العمل (Codespaces URL)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SasaPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = SasaPrimary,
                        focusedTextColor = SasaTextPrimary,
                        unfocusedTextColor = SasaTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("رمز الوصول (Access Token / Secret)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SasaPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = SasaPrimary,
                        focusedTextColor = SasaTextPrimary,
                        unfocusedTextColor = SasaTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفعيل خدمة التنفيذ الخلفية الشفافة",
                        fontSize = 14.sp,
                        color = SasaTextPrimary
                    )
                    Switch(
                        checked = isBgServiceActive,
                        onCheckedChange = { isBgServiceActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SasaPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المزامنة التلقائية مع السحابة",
                        fontSize = 14.sp,
                        color = SasaTextPrimary
                    )
                    Switch(
                        checked = isAutoSync,
                        onCheckedChange = { isAutoSync = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SasaPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val newConfig = CloudWorkspaceConfig(
                                workspaceUrl = workspaceUrl,
                                token = token,
                                environmentName = envName,
                                isAutoSyncEnabled = isAutoSync,
                                isBackgroundServiceActive = isBgServiceActive
                            )
                            onSaveConfig(newConfig)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SasaPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ وتفعيل", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            val currentConfig = CloudWorkspaceConfig(
                                workspaceUrl = workspaceUrl,
                                token = token,
                                environmentName = envName,
                                isAutoSyncEnabled = isAutoSync,
                                isBackgroundServiceActive = isBgServiceActive
                            )
                            onTestConnection(currentConfig)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = SasaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اختبار السكربت", color = SasaPrimary)
                    }
                }

                // Active Background Tasks Monitor
                if (backgroundTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "الخدمات الخلفية النشطة الان:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SasaPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        items(backgroundTasks.values.toList()) { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (task.isRunning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = SasaPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.Green,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = task.taskName,
                                            fontSize = 12.sp,
                                            color = SasaTextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        task.progressMessage?.let { msg ->
                                            Text(
                                                text = msg,
                                                fontSize = 11.sp,
                                                color = SasaTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
