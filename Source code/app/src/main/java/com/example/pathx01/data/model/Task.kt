package com.example.pathx01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val category: String = "General",
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val dueDate: String? = null,
    val subtasks: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Serializable
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
