package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackgroundTaskStatus(
    val taskId: String,
    val taskName: String,
    val isRunning: Boolean,
    val progressMessage: String? = null,
    val isSuccess: Boolean? = null
)

object BackgroundServiceManager {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val _tasks = MutableStateFlow<Map<String, BackgroundTaskStatus>>(emptyMap())
    val tasks: StateFlow<Map<String, BackgroundTaskStatus>> = _tasks.asStateFlow()

    fun runTransparentTask(
        taskId: String,
        taskName: String,
        action: suspend () -> Unit
    ) {
        scope.launch {
            try {
                _tasks.value = _tasks.value + (taskId to BackgroundTaskStatus(
                    taskId = taskId,
                    taskName = taskName,
                    isRunning = true,
                    progressMessage = "جاري تنفيذ الخدمة الخلفية..."
                ))
                action()
                _tasks.value = _tasks.value + (taskId to BackgroundTaskStatus(
                    taskId = taskId,
                    taskName = taskName,
                    isRunning = false,
                    progressMessage = "تم اكتمال المهام الخلفية بنجاح",
                    isSuccess = true
                ))
            } catch (e: Exception) {
                _tasks.value = _tasks.value + (taskId to BackgroundTaskStatus(
                    taskId = taskId,
                    taskName = taskName,
                    isRunning = false,
                    progressMessage = "خطأ بالخدمة الخلفية: ${e.message}",
                    isSuccess = false
                ))
            }
        }
    }
}
