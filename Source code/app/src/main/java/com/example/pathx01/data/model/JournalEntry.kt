package com.example.pathx01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String? = null,
    val content: String,
    val mood: Mood? = null,
    val tags: String? = null, // Comma-separated tags
    val date: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class Mood {
    HAPPY,
    TIRED,
    MOTIVATED,
    STRESSED,
    CONTENT,
    EXCITED,
    ANXIOUS,
    PROUD,
    FRUSTRATED,
    GRATEFUL
}
