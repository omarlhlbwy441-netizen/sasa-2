package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SasaCodeBackground
import com.example.ui.theme.SasaPrimary
import com.example.ui.theme.SasaTextSecondary

@Composable
fun MessageTextWithCodeBlocks(
    text: String,
    onPushToCloud: ((filePath: String, content: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val parts = parseMarkdownCodeBlocks(text)

    Column(modifier = modifier) {
        parts.forEach { part ->
            when (part) {
                is TextPart.NormalText -> {
                    if (part.text.isNotBlank()) {
                        Text(
                            text = part.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                is TextPart.CodeBlock -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SasaCodeBackground)
                            .border(1.dp, SasaPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        // Header bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SasaPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = part.language.ifBlank { "code" },
                                style = MaterialTheme.typography.labelSmall,
                                color = SasaPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(part.code))
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(32.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "نسخ الكود",
                                        tint = SasaTextSecondary,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }
                        }

                        // Code content
                        Text(
                            text = part.code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFE0E6ED),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

sealed class TextPart {
    data class NormalText(val text: String) : TextPart()
    data class CodeBlock(val language: String, val code: String) : TextPart()
}

private fun parseMarkdownCodeBlocks(rawText: String): List<TextPart> {
    val result = mutableListOf<TextPart>()
    val regex = Regex("```(\\w*)\\n([\\s\\S]*?)```")
    var lastIndex = 0

    regex.findAll(rawText).forEach { matchResult ->
        val textBefore = rawText.substring(lastIndex, matchResult.range.first)
        if (textBefore.isNotEmpty()) {
            result.add(TextPart.NormalText(textBefore))
        }
        val lang = matchResult.groupValues[1]
        val code = matchResult.groupValues[2].trimEnd()
        result.add(TextPart.CodeBlock(lang, code))
        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < rawText.length) {
        val remaining = rawText.substring(lastIndex)
        result.add(TextPart.NormalText(remaining))
    }

    return result
}
