package com.example.pathx01.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "writing_entries")
data class WritingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String,
    val mood: String?,
    val tagsJson: String?,
    val attachmentsJson: String?,
    val checklistsJson: String?,
    val createdAt: LocalDateTime
)


