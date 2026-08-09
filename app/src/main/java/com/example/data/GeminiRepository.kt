package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val text: String, val modelUsed: GeminiModel) : GeminiResult()
    data class QuotaExceeded(val message: String, val modelTried: GeminiModel) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
}

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun sanitizeKey(key: String): String {
        return key.trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .removePrefix("API_KEY=")
            .removePrefix("GEMINI_API_KEY=")
            .trim()
    }

    private fun isValidKeyCandidate(key: String): Boolean {
        if (key.isBlank()) return false
        if (key == "MY_GEMINI_API_KEY") return false
        return true
    }

    suspend fun generateContentWithFailover(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        preferredModel: GeminiModel = GeminiModel.FLASH_3_6,
        customApiKey: String? = null
    ): GeminiResult = withContext(Dispatchers.IO) {

        val keysToTry = mutableListOf<String>()

        // 1. User custom key if provided
        if (!customApiKey.isNullOrBlank()) {
            val cleanCustom = sanitizeKey(customApiKey)
            if (isValidKeyCandidate(cleanCustom)) {
                keysToTry.add(cleanCustom)
            }
        }

        // 2. BuildConfig / Env keys
        val defaultConfigKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        val envKey = try { System.getenv("GEMINI_API_KEY") ?: System.getenv("API_KEY") ?: "" } catch (e: Exception) { "" }

        val cleanConfig = sanitizeKey(defaultConfigKey)
        if (isValidKeyCandidate(cleanConfig) && !keysToTry.contains(cleanConfig)) {
            keysToTry.add(cleanConfig)
        }

        val cleanEnv = sanitizeKey(envKey)
        if (isValidKeyCandidate(cleanEnv) && !keysToTry.contains(cleanEnv)) {
            keysToTry.add(cleanEnv)
        }

        // Models ordered starting from preferredModel
        val modelsOrder = mutableListOf<GeminiModel>()
        modelsOrder.add(preferredModel)
        GeminiModel.entries.forEach { m ->
            if (m != preferredModel) modelsOrder.add(m)
        }

        if (keysToTry.isEmpty()) {
            return@withContext GeminiResult.Error(
                "💡 لم يتم تكوين مفتاح Gemini API صالح في التطبيق. يرجى التأكد من إضافة GEMINI_API_KEY عبر Secrets Panel أو ملف .env بـ Google AI Studio."
            )
        }

        var lastError: GeminiResult = GeminiResult.Error("فشل الاتصال بجميع نماذج Gemini.")

        for (apiKey in keysToTry) {
            for (model in modelsOrder) {
                try {
                    val result = executeGeminiRequest(prompt, conversationHistory, model, apiKey)
                    if (result is GeminiResult.Success) {
                        return@withContext result
                    } else {
                        lastError = result
                    }
                } catch (e: Exception) {
                    lastError = GeminiResult.Error("خطأ في الاتصال بالشبكة: ${e.message}")
                }
            }
        }

        lastError
    }

    private fun executeGeminiRequest(
        prompt: String,
        history: List<ChatMessage>,
        model: GeminiModel,
        apiKey: String
    ): GeminiResult {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.id}:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Include recent history (up to last 10 turns) to manage context window
        val recentTurns = history.takeLast(10)
        recentTurns.forEach { msg ->
            if (msg.sender == MessageSender.USER || msg.sender == MessageSender.SASA_AI) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val turnObj = JSONObject()
                turnObj.put("role", role)
                val partsArr = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", msg.text)
                partsArr.put(partObj)
                turnObj.put("parts", partsArr)
                contentsArray.put(turnObj)
            }
        }

        // Current user prompt
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()
        val currentPart = JSONObject()
        currentPart.put("text", prompt)
        currentParts.put(currentPart)
        currentTurn.put("parts", currentParts)
        contentsArray.put(currentTurn)

        val requestJson = JSONObject()
        requestJson.put("contents", contentsArray)

        // System Instruction
        val sysInst = JSONObject()
        val sysInstParts = JSONArray()
        val sysInstText = JSONObject()
        sysInstText.put(
            "text",
            "أنت منظومة 'صاصا AI' (Sasa AI v15.2)، مساعد ذكاء اصطناعي برمجي وعام متقدم ومحترف باللغة العربية. " +
                    "عند كتابة أكواد برمجية، اكتب كوداً كاملاً ومحترفاً بدون اختصارات أو تعليقات ناقصة، مع تقديم شرح واضح ومفصل للخطوات والعناصر التوضيحية."
        )
        sysInstParts.put(sysInstText)
        sysInst.put("parts", sysInstParts)
        requestJson.put("systemInstruction", sysInst)

        // Generation config for maximum completeness
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        genConfig.put("maxOutputTokens", 8192)
        requestJson.put("generationConfig", genConfig)

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("x-goog-api-key", apiKey)

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                if (response.code == 429 || responseBodyString.contains("Quota limits exceeded", ignoreCase = true) || responseBodyString.contains("RESOURCE_EXHAUSTED", ignoreCase = true)) {
                    return GeminiResult.QuotaExceeded(
                        "تجاوز حد الاستخدام (Quota Exceeded) للنموذج ${model.displayName}.",
                        model
                    )
                }
                val errorMsg = parseErrorMessage(responseBodyString)
                return GeminiResult.Error("خطأ (${response.code}): $errorMsg")
            }

            val parsedText = parseCandidateText(responseBodyString)
            return if (parsedText.isNotBlank()) {
                GeminiResult.Success(parsedText, model)
            } else {
                GeminiResult.Error("لم يتم استلام رد نصي من النموذج ${model.displayName}.")
            }
        }
    }

    private fun parseCandidateText(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val text = part.optString("text", "")
                sb.append(text)
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseErrorMessage(jsonStr: String): String {
        return try {
            if (jsonStr.contains("API keys are not supported") || jsonStr.contains("API key not valid") || jsonStr.contains("code=401") || jsonStr.contains("401")) {
                return "مفتاح Gemini API الحالي غير صحيح أو منتهي الصلاحية. يرجى التحقق من المفتاح في إعدادات التطبيق."
            }
            val root = JSONObject(jsonStr)
            val error = root.optJSONObject("error")
            val message = error?.optString("message", "") ?: ""
            if (message.contains("API keys are not supported") || message.contains("API key not valid")) {
                "مفتاح Gemini API غير صالح."
            } else if (message.isNotBlank()) {
                message
            } else {
                "حدث خطأ في استجابة الخادم"
            }
        } catch (e: Exception) {
            "فشل الاتصال بالخادم. يرجى التحقق من مفتاح API وإعدادات الشبكة."
        }
    }
}
