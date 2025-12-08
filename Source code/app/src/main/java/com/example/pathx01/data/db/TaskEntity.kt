package com.example.pathx01.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val category: String,
    val priority: String,
    val dueDate: String?,
    val subtasksJson: String?,
    val isCompleted: Boolean
)


