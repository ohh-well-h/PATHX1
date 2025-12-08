package com.example.pathx01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: ProjectCategory,
    val progressPercentage: Int = 0,
    val isCompleted: Boolean = false,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val targetDate: LocalDateTime? = null,
    val completedDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ProjectCategory {
    ACADEMIC,
    PERSONAL,
    CAREER,
    EXTRACURRICULAR,
    OTHER
}
