package com.example.pathx01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "task_categories")
data class TaskCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: String = "#2196F3", // Default blue color
    val isDefault: Boolean = false, // true for built-in categories like IA, EE, SAT
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Default categories that come with the app
object DefaultTaskCategories {
    val IA = TaskCategory(name = "IA", color = "#FF5722", isDefault = true)
    val EE = TaskCategory(name = "Extended Essay", color = "#9C27B0", isDefault = true)
    val SAT = TaskCategory(name = "SAT Prep", color = "#FF9800", isDefault = true)
    val ASSIGNMENT = TaskCategory(name = "Assignment", color = "#4CAF50", isDefault = true)
    val UNIVERSITY = TaskCategory(name = "University Deadline", color = "#F44336", isDefault = true)
    val PERSONAL = TaskCategory(name = "Personal", color = "#607D8B", isDefault = true)
    
    fun getAllDefault(): List<TaskCategory> = listOf(IA, EE, SAT, ASSIGNMENT, UNIVERSITY, PERSONAL)
}
