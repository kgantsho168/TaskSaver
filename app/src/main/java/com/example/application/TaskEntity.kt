package com.example.application

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "General", // e.g., Work, Personal, Study
    val priority: String = "Medium",  // Low, Medium, High
    val isCompleted: Boolean = false,
    val dueDateMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)