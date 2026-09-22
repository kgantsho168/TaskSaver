package com.example.application

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val dueDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false
)