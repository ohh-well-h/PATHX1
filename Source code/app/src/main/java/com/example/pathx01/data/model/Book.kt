package com.example.pathx01.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Book(
    val id: Int = 0,
    val title: String,
    val author: String,
    val status: BookStatus = BookStatus.TO_READ,
    val genre: String? = null,
    val totalPages: Int? = null,
    val pagesRead: Int = 0,
    val rating: Int? = null, // 1-5 stars
    val notes: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startedDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completedDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Serializable
enum class BookStatus {
    TO_READ,
    READING,
    COMPLETED,
    DNF // Did Not Finish
}
