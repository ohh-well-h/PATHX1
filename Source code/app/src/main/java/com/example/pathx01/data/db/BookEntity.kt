package com.example.pathx01.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val status: String,
    val totalPages: Int,
    val pagesRead: Int,
    val rating: Int?,
    val startedDate: LocalDateTime?,
    val completedDate: LocalDateTime?
)


