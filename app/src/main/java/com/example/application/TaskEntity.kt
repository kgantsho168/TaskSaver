package com.example.application

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: String = "Medium", // "High", "Medium", "Low"
    val dueDate: String = "",
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false
)