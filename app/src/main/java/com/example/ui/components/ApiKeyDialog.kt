package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SasaCardBackground
import com.example.ui.theme.SasaDarkSurface
import com.example.ui.theme.SasaPrimary
import com.example.ui.theme.SasaSecondary
import com.example.ui.theme.SasaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyDialog(
    currentKey: String,
    onSaveKey: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember(currentKey) { mutableStateOf(currentKey) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("api_key_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = SasaDarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "مفتاح API",
                            tint = SasaPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "إعداد مفتاح Gemini API",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SasaTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "يمكنك أدخال مفتاح Gemini API الخاص بك للحصول على سرعة استجابة أعلى وتجنب حدود الاستخدام المترددة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SasaTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SasaSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_text_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SasaPrimary,
                        unfocusedBorderColor = SasaCardBackground,
                        focusedLabelColor = SasaPrimary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = SasaTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveKey(apiKeyInput)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SasaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_api_key_button")
                    ) {
                        Text("حفظ المفتاح", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
