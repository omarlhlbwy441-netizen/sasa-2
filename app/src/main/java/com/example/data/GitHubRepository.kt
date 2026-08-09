package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64
import java.util.concurrent.TimeUnit

data class GitHubRepoItem(
    val name: String,
    val fullName: String,
    val isPrivate: Boolean,
    val defaultBranch: String,
    val htmlUrl: String,
    val description: String?
)

data class GitHubTreeItem(
    val path: String,
    val type: String, // "blob" or "tree"
    val size: Long = 0
)

data class GitHubFileContent(
    val path: String,
    val content: String,
    val sha: String,
    val htmlUrl: String?
)

sealed class GitHubResult<out T> {
    data class Success<out T>(val data: T) : GitHubResult<T>()
    data class Error(val message: String) : GitHubResult<Nothing>()
}

class GitHubRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun buildRequest(url: String, token: String, method: String = "GET", bodyJson: String? = null): Request {
        val builder = Request.Builder()
            .url(url)
            .header("User-Agent", "SasaAI-Android")
            .header("Accept", "application/vnd.github.v3+json")

        if (token.isNotBlank()) {
            builder.header("Authorization", "Bearer ${token.trim()}")
        }

        if (method == "POST" || method == "PUT") {
            val body = (bodyJson ?: "{}").toRequestBody(jsonMediaType)
            if (method == "POST") builder.post(body)
            else builder.put(body)
        }

        return builder.build()
    }

    suspend fun verifyToken(token: String): GitHubResult<String> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("https://api.github.com/user", token)
            client.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string() ?: ""
                if (resp.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    val login = json.optString("login", "مستخدم")
                    GitHubResult.Success("تم التحقق بنجاح! المستخدم: $login")
                } else {
                    GitHubResult.Error("فشل التوثيق (رمز ${resp.code}): ${resp.message}")
                }
            }
        } catch (e: Exception) {
            GitHubResult.Error("خطأ في الاتصال بـ GitHub: ${e.message}")
        }
    }

    suspend fun getUserRepos(token: String): GitHubResult<List<GitHubRepoItem>> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("https://api.github.com/user/repos?per_page=100&sort=updated", token)
            client.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string() ?: ""
                if (resp.isSuccessful) {
                    val jsonArr = JSONArray(bodyStr)
                    val list = mutableListOf<GitHubRepoItem>()
                    for (i in 0 until jsonArr.length()) {
                        val item = jsonArr.getJSONObject(i)
                        list.add(
                            GitHubRepoItem(
                                name = item.getString("name"),
                                fullName = item.getString("full_name"),
                                isPrivate = item.optBoolean("private", false),
                                defaultBranch = item.optString("default_branch", "main"),
                                htmlUrl = item.optString("html_url", ""),
                                description = item.optString("description", "")
                            )
                        )
                    }
                    GitHubResult.Success(list)
                } else {
                    GitHubResult.Error("فشل جلب المستودعات (${resp.code})")
                }
            }
        } catch (e: Exception) {
            GitHubResult.Error("خطأ: ${e.message}")
        }
    }

    suspend fun getRepoTree(token: String, owner: String, repo: String, branch: String = "main"): GitHubResult<List<GitHubTreeItem>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$owner/$repo/git/trees/$branch?recursive=1"
            val req = buildRequest(url, token)
            client.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string() ?: ""
                if (resp.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    val treeArr = json.optJSONArray("tree") ?: JSONArray()
                    val list = mutableListOf<GitHubTreeItem>()
                    for (i in 0 until treeArr.length()) {
                        val item = treeArr.getJSONObject(i)
                        list.add(
                            GitHubTreeItem(
                                path = item.getString("path"),
                                type = item.optString("type", "blob"),
                                size = item.optLong("size", 0L)
                            )
                        )
                    }
                    GitHubResult.Success(list)
                } else {
                    GitHubResult.Error("تعذر فحص شجرة المستودع (${resp.code})")
                }
            }
        } catch (e: Exception) {
            GitHubResult.Error("خطأ في فحص المستودع: ${e.message}")
        }
    }

    suspend fun getFileContent(token: String, owner: String, repo: String, path: String, branch: String = "main"): GitHubResult<GitHubFileContent> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$owner/$repo/contents/$path?ref=$branch"
            val req = buildRequest(url, token)
            client.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string() ?: ""
                if (resp.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    val sha = json.getString("sha")
                    val rawBase64 = json.optString("content", "").replace("\n", "").replace("\r", "")
                    val htmlUrl = json.optString("html_url", "")
                    
                    val decoded = try {
                        String(Base64.getDecoder().decode(rawBase64), Charsets.UTF_8)
                    } catch (e: Exception) {
                        "[محتوى ثنائي أو غير قابل للفك]"
                    }

                    GitHubResult.Success(
                        GitHubFileContent(
                            path = path,
                            content = decoded,
                            sha = sha,
                            htmlUrl = htmlUrl
                        )
                    )
                } else {
                    GitHubResult.Error("تعذر فتح الملف ($path) - رمز ${resp.code}")
                }
            }
        } catch (e: Exception) {
            GitHubResult.Error("خطأ قراءة الملف: ${e.message}")
        }
    }

    suspend fun commitFileChange(
        token: String,
        owner: String,
        repo: String,
        path: String,
        newContent: String,
        commitMessage: String,
        sha: String?,
        branch: String = "main"
    ): GitHubResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$owner/$repo/contents/$path"
            val encodedContent = Base64.getEncoder().encodeToString(newContent.toByteArray(Charsets.UTF_8))
            
            val jsonPayload = JSONObject().apply {
                put("message", if (commitMessage.isBlank()) "تحديث عبر Sasa AI" else commitMessage)
                put("content", encodedContent)
                put("branch", branch)
                if (!sha.isNullOrBlank()) {
                    put("sha", sha)
                }
            }

            val req = buildRequest(url, token, method = "PUT", bodyJson = jsonPayload.toString())
            client.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string() ?: ""
                if (resp.isSuccessful) {
                    GitHubResult.Success("تم حفظ التعديل والالتزام (Commit) بنجاح على GitHub!")
                } else {
                    GitHubResult.Error("فشل حفظ التعديل (${resp.code}): ${resp.message}")
                }
            }
        } catch (e: Exception) {
            GitHubResult.Error("خطأ أثناء Commit: ${e.message}")
        }
    }

    suspend fun forkRepo(token: String, owner: String, repo: String): GitHubResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$owner/$repo/forks"
            val req = buildRequest(url, token, method = "POST", bodyJson = "{}")
            client.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string() ?: ""
                if (resp.isSuccessful || resp.code == 202) {
                    val json = JSONObject(bodyStr)
                    val full = json.optString("full_name", "$owner/$repo")
                    GitHubResult.Success("تم نسخ/تفريغ المستودع بنجاح إلى حسابك: $full")
                } else {
                    GitHubResult.Error("فشل نسخ المستودع (${resp.code})")
                }
            }
        } catch (e: Exception) {
            GitHubResult.Error("خطأ أثناء النسخ: ${e.message}")
        }
    }

    suspend fun resolveGitHubContext(prompt: String, token: String): String? = withContext(Dispatchers.IO) {
        val githubRegex = Regex("""https?://github\.com/([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)(?:/blob/([^/]+)/(.+)|/tree/([^/]+)/(.+))?""")
        val match = githubRegex.find(prompt) ?: return@withContext null

        val owner = match.groupValues[1]
        val repo = match.groupValues[2].removeSuffix(".git")
        val blobBranch = match.groupValues.getOrNull(3)
        val blobPath = match.groupValues.getOrNull(4)

        if (!blobPath.isNullOrBlank() && !blobBranch.isNullOrBlank()) {
            // Specific file URL
            when (val fileRes = getFileContent(token, owner, repo, blobPath, blobBranch)) {
                is GitHubResult.Success -> {
                    return@withContext """
--- 📄 محتوى ملف GitHub المجلوب تلقائياً ($owner/$repo - $blobPath) ---
${fileRes.data.content}
----------------------------------------------------------------------
""".trimIndent()
                }
                else -> {}
            }
        } else {
            // Repo level URL
            val sb = StringBuilder()
            sb.appendLine("--- 📦 بيانات وسياق مستودع GitHub المجلوب تلقائياً ($owner/$repo) ---")

            // Try fetching README
            when (val readmeRes = getFileContent(token, owner, repo, "README.md", "main")) {
                is GitHubResult.Success -> {
                    sb.appendLine("📖 [محتوى README.md]:")
                    sb.appendLine(readmeRes.data.content.take(4000))
                }
                else -> {
                    when (val readmeResAlt = getFileContent(token, owner, repo, "README.md", "master")) {
                        is GitHubResult.Success -> {
                            sb.appendLine("📖 [محتوى README.md]:")
                            sb.appendLine(readmeResAlt.data.content.take(4000))
                        }
                        else -> {}
                    }
                }
            }

            // Fetch Tree
            when (val treeRes = getRepoTree(token, owner, repo, "main")) {
                is GitHubResult.Success -> {
                    val files = treeRes.data.filter { it.type == "blob" }.take(30).joinToString("\n") { "  - ${it.path}" }
                    sb.appendLine("📁 [قائمة وأهم ملفات المستودع]:\n$files")
                }
                else -> {}
            }

            sb.appendLine("----------------------------------------------------------------------")
            return@withContext sb.toString()
        }

        return@withContext null
    }
}
