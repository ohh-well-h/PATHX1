package com.example.pathx01.data.repository

import com.example.pathx01.data.dao.TaskDao
import com.example.pathx01.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class PathXRepository constructor(
    private val taskDao: TaskDao,
    private val projectDao: com.example.pathx01.data.dao.ProjectDao?,
    private val bookDao: com.example.pathx01.data.dao.BookDao?,
    private val journalDao: com.example.pathx01.data.dao.JournalDao?
) {
    // Task operations
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    fun getIncompleteTasks(): Flow<List<Task>> = taskDao.getIncompleteTasks()
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    fun getTasksByCategory(category: String): Flow<List<Task>> = taskDao.getTasksByCategory(category)
    fun getAllCategories(): Flow<List<String>> = taskDao.getAllCategories()

    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun updateTaskCompletion(id: Int, isCompleted: Boolean) =
        taskDao.updateTaskCompletion(id, isCompleted, LocalDateTime.now().toString())
}
