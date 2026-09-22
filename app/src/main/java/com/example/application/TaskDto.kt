package com.example.application

data class TaskDto(
    val id: String? = null,
    val title: String,
    val description: String,
    val priority: String,
    val isCompleted: Boolean
)