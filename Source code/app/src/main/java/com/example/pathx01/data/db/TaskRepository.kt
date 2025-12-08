package com.example.pathx01.data.db

import android.content.Context
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.model.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).taskDao()

    suspend fun getAll(): List<Task> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.toModel() }
    }

    suspend fun insert(task: Task): Int = withContext(Dispatchers.IO) {
        dao.insert(task.toEntity()).toInt()
    }

    suspend fun update(task: Task) = withContext(Dispatchers.IO) {
        dao.update(task.toEntity())
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    private fun TaskEntity.toModel(): Task = Task(
        id = id,
        title = title,
        description = description,
        category = category,
        priority = Priority.valueOf(priority),
        dueDate = dueDate,
        subtasks = subtasksJson,
        isCompleted = isCompleted
    )

    private fun Task.toEntity(): TaskEntity = TaskEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        priority = priority.name,
        dueDate = dueDate,
        subtasksJson = subtasks,
        isCompleted = isCompleted
    )
}


