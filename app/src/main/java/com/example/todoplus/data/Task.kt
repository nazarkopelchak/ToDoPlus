package com.example.todoplus.data

data class Task(
    val taskName: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val priority: Priorities,
    val taskId: Int
) : java.io.Serializable
