package com.example.pathx01.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Subtask(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false
)
