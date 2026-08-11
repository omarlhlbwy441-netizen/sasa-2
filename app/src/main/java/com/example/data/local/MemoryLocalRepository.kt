package com.example.data.local

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MemoryLocalRepository(private val memoryDao: ProjectMemoryDao) {

    val allMemories: Flow<List<ProjectMemoryEntity>> = memoryDao.getAllMemoriesFlow()

    suspend fun getMemoriesList(): List<ProjectMemoryEntity> {
        return memoryDao.getAllMemories()
    }

    suspend fun saveMemory(key: String, content: String) {
        val entity = ProjectMemoryEntity(
            id = UUID.randomUUID().toString(),
            key = key,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        memoryDao.insertMemory(entity)
    }

    suspend fun deleteMemoryByKey(key: String) {
        memoryDao.deleteByKey(key)
    }

    suspend fun clearMemories() {
        memoryDao.clearAllMemories()
    }
}
