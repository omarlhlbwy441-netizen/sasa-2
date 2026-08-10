package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class GeminiModel(
    val id: String,
    val displayName: String,
    val description: String
) {
    FLASH_3_6("gemini-2.0-flash", "3.6 Flash", "مساعدة متكاملة، البرمجة والتحليل اليومي"),
    FLASH_LITE_3_5("gemini-2.0-flash-lite", "3.5 Flash-Lite", "أسرع الإجابات والردود الفورية"),
    PRO_3_1("gemini-1.5-pro", "3.1 Pro", "الرياضيات والبرمجة المتقدمة والتحليل العميق"),
    THINKING_EXP("gemini-2.0-flash-thinking-exp", "تفكير موسّع", "حل المشاكل المعقدة والتخطيط البرمجي المنطقي")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val isError: Boolean = false,
    val isSystemNotice: Boolean = false,
    val codeBlocks: List<CodeBlock> = emptyList(),
    val generatedFiles: List<GeneratedFile> = emptyList()
)

enum class MessageSender {
    USER,
    SASA_AI,
    SYSTEM
}

data class ApiKeyStatus(
    val isCustom: Boolean,
    val keyPreview: String
)

@JsonClass(generateAdapter = true)
data class CodeBlock(
    @Json(name = "language") val language: String = "text",
    @Json(name = "filename") val filename: String? = null,
    @Json(name = "code") val code: String = ""
)

@JsonClass(generateAdapter = true)
data class GeneratedFile(
    @Json(name = "filename") val filename: String,
    @Json(name = "file_type") val fileType: String,
    @Json(name = "content") val content: String,
    @Json(name = "path") val path: String = filename,
    @Json(name = "size_bytes") val sizeBytes: Long = content.toByteArray().size.toLong()
)

@JsonClass(generateAdapter = true)
data class SasaBackendChatRequest(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "model") val model: String = "gemini-2.0-flash",
    @Json(name = "custom_api_key") val customApiKey: String? = null,
    @Json(name = "github_token") val githubToken: String? = null,
    @Json(name = "include_repo_context") val includeRepoContext: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SasaBackendChatResponse(
    @Json(name = "status") val status: String,
    @Json(name = "response_text") val responseText: String,
    @Json(name = "model_used") val modelUsed: String,
    @Json(name = "code_blocks") val codeBlocks: List<CodeBlock> = emptyList(),
    @Json(name = "files_created") val filesCreated: List<GeneratedFile> = emptyList()
)

@JsonClass(generateAdapter = true)
data class FileGenerationRequest(
    @Json(name = "filename") val filename: String,
    @Json(name = "file_type") val fileType: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "target_path") val targetPath: String = filename
)

@JsonClass(generateAdapter = true)
data class FileGenerationResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "generated_file") val generatedFile: GeneratedFile?,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class CloudPushRequest(
    @Json(name = "github_token") val githubToken: String,
    @Json(name = "owner") val owner: String,
    @Json(name = "repo") val repo: String,
    @Json(name = "file_path") val filePath: String,
    @Json(name = "content") val content: String,
    @Json(name = "commit_message") val commitMessage: String,
    @Json(name = "branch") val branch: String = "main"
)

@JsonClass(generateAdapter = true)
data class CloudPushResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "commit_sha") val commitSha: String?,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class GitHubRepoScanResult(
    @Json(name = "owner") val owner: String,
    @Json(name = "repo") val repo: String,
    @Json(name = "total_files") val totalFiles: Int,
    @Json(name = "file_list") val fileList: List<String>,
    @Json(name = "readme_summary") val readmeSummary: String?
)

@JsonClass(generateAdapter = true)
data class MediaGenerationRequest(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "media_type") val mediaType: String = "IMAGE", // IMAGE, AUDIO_SPEECH, DIAGRAM_SVG
    @Json(name = "aspect_ratio") val aspectRatio: String = "1:1",
    @Json(name = "style") val style: String = "photorealistic"
)

@JsonClass(generateAdapter = true)
data class MediaGenerationResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "data_url") val dataUrl: String? = null,
    @Json(name = "mime_type") val mimeType: String = "image/png",
    @Json(name = "description") val description: String = "",
    @Json(name = "message") val message: String = ""
)

@JsonClass(generateAdapter = true)
data class MediaProcessRequest(
    @Json(name = "operation") val operation: String, // OCR, TRANSCRIPTION, COMPRESS, FORMAT_CONVERT
    @Json(name = "media_base64") val mediaBase64: String,
    @Json(name = "target_format") val targetFormat: String = "png"
)

@JsonClass(generateAdapter = true)
data class MediaProcessResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "processed_base64") val processedBase64: String? = null,
    @Json(name = "extracted_text") val extractedText: String? = null,
    @Json(name = "metadata") val metadata: Map<String, String> = emptyMap()
)

