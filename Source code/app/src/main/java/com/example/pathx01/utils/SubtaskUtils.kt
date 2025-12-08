package com.example.pathx01.utils

import com.example.pathx01.data.model.Subtask
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

object SubtaskUtils {
    private val json = Json { ignoreUnknownKeys = true }
    
    fun subtasksToJson(subtasks: List<Subtask>): String {
        return try {
            json.encodeToString(subtasks)
        } catch (e: Exception) {
            ""
        }
    }
    
    fun jsonToSubtasks(jsonString: String?): List<Subtask> {
        return try {
            if (jsonString.isNullOrBlank()) return emptyList()
            json.decodeFromString<List<Subtask>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun createSubtask(text: String): Subtask {
        return Subtask(
            id = UUID.randomUUID().toString(),
            text = text,
            isCompleted = false
        )
    }
    
    fun updateSubtask(subtasks: List<Subtask>, id: String, text: String? = null, isCompleted: Boolean? = null): List<Subtask> {
        return subtasks.map { subtask ->
            if (subtask.id == id) {
                subtask.copy(
                    text = text ?: subtask.text,
                    isCompleted = isCompleted ?: subtask.isCompleted
                )
            } else {
                subtask
            }
        }
    }
    
    fun deleteSubtask(subtasks: List<Subtask>, id: String): List<Subtask> {
        return subtasks.filter { it.id != id }
    }
}
