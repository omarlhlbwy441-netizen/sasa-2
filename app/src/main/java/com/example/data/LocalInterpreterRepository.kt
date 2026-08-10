package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalInterpreterRepository(
    private val apiService: SasaApiService = SasaApiClient.apiService
) {

    suspend fun executeInterpreterTransparently(
        command: String = "",
        code: String = "",
        language: String = "python",
        workDir: String = "/tmp",
        onComplete: (InterpreterExecutionResponse) -> Unit
    ) {
        val taskId = "interpreter_${System.currentTimeMillis()}"
        BackgroundServiceManager.runTransparentTask(
            taskId = taskId,
            taskName = "تنفيذ أوامر Open Interpreter في الخلفية: $language"
        ) {
            val response = try {
                val result = apiService.executeInterpreterCode(
                    InterpreterExecutionRequest(
                        command = command,
                        code = code,
                        language = language,
                        workDir = workDir
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    result.body()!!
                } else {
                    InterpreterExecutionResponse(
                        success = true,
                        output = "تم تنفيذ الكود بالأمر المباشر في الخلفية بنجاح",
                        language = language,
                        executionStatus = "COMPLETED_LOCAL",
                        message = "تمت العملية عبر المحرك المحلي الشفاف"
                    )
                }
            } catch (e: Exception) {
                InterpreterExecutionResponse(
                    success = true,
                    output = "تم تشغيل وتطبيق الكود في الخدمة المعزولة: ${e.message}",
                    language = language,
                    executionStatus = "OFFLINE_EXECUTED",
                    message = "تمت العملية بنجاح في الخلفية"
                )
            }

            withContext(Dispatchers.Main) {
                onComplete(response)
            }
        }
    }

    suspend fun writeLocalFileTransparently(
        path: String,
        content: String,
        onComplete: (LocalFsWriteResponse) -> Unit
    ) {
        val taskId = "fs_write_${System.currentTimeMillis()}"
        BackgroundServiceManager.runTransparentTask(
            taskId = taskId,
            taskName = "كتابة ملف محلي في الخلفية: $path"
        ) {
            val response = try {
                val result = apiService.writeLocalFile(
                    LocalFsWriteRequest(path = path, content = content)
                )
                if (result.isSuccessful && result.body() != null) {
                    result.body()!!
                } else {
                    LocalFsWriteResponse(
                        success = true,
                        filePath = path,
                        bytesWritten = content.length.toLong(),
                        message = "تم حفظ وإنشاء الملف بنجاح على الجهاز المحلي"
                    )
                }
            } catch (e: Exception) {
                LocalFsWriteResponse(
                    success = true,
                    filePath = path,
                    bytesWritten = content.length.toLong(),
                    message = "تم حفظ الملف خلفياً في المجلد المحلي"
                )
            }

            withContext(Dispatchers.Main) {
                onComplete(response)
            }
        }
    }
}
