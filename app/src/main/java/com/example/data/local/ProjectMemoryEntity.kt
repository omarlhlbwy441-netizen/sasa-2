package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_memories")
data class ProjectMemoryEntity(
    @PrimaryKey
    val id: String,
    val key: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
