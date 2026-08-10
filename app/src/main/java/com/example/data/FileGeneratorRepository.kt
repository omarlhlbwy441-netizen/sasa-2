package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileGeneratorRepository {

    suspend fun generateFile(
        filename: String,
        fileType: String,
        content: String,
        targetPath: String = filename
    ): Result<GeneratedFile> = withContext(Dispatchers.IO) {
        try {
            // First try calling backend service
            val request = FileGenerationRequest(
                filename = filename,
                fileType = fileType,
                prompt = content,
                targetPath = targetPath
            )
            val response = SasaApiClient.apiService.generateFile(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.generatedFile != null) {
                return@withContext Result.success(response.body()!!.generatedFile!!)
            }

            // Local fallback file creation logic
            val created = GeneratedFile(
                filename = filename,
                fileType = fileType,
                content = content,
                path = targetPath,
                sizeBytes = content.toByteArray().size.toLong()
            )
            Result.success(created)
        } catch (e: Exception) {
            val fallback = GeneratedFile(
                filename = filename,
                fileType = fileType,
                content = content,
                path = targetPath,
                sizeBytes = content.toByteArray().size.toLong()
            )
            Result.success(fallback)
        }
    }

    suspend fun pushFileToCloudRepo(
        githubToken: String,
        owner: String,
        repo: String,
        filePath: String,
        content: String,
        commitMessage: String,
        branch: String = "main"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = CloudPushRequest(
                githubToken = githubToken,
                owner = owner,
                repo = repo,
                filePath = filePath,
                content = content,
                commitMessage = commitMessage,
                branch = branch
            )
            val response = SasaApiClient.apiService.pushToCloudRepo(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val sha = response.body()?.commitSha ?: "success"
                return@withContext Result.success("تم رفع التحديث بنجاح إلى المستودع السحابي ($owner/$repo) - SHA: $sha")
            }
            Result.failure(Exception(response.body()?.message ?: "فشل الرفع عبر الخادم الخلفي"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
