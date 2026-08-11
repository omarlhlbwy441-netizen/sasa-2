package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectMemoryDao {

    @Query("SELECT * FROM project_memories ORDER BY timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<ProjectMemoryEntity>>

    @Query("SELECT * FROM project_memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<ProjectMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: ProjectMemoryEntity)

    @Query("DELETE FROM project_memories WHERE key = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM project_memories")
    suspend fun clearAllMemories()
}
