package com.example.pathx01.data.repository

import com.example.pathx01.data.dao.ProjectTodoDao
import com.example.pathx01.data.model.ProjectTodo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class ProjectTodoRepository constructor(
    private val projectTodoDao: ProjectTodoDao
) {
    
    fun getTodosByProject(projectId: Int): Flow<List<ProjectTodo>> = projectTodoDao.getTodosByProject(projectId)
    
    suspend fun getTodoById(id: Int): ProjectTodo? = projectTodoDao.getTodoById(id)
    
    fun getTotalTodosCount(projectId: Int): Flow<Int> = projectTodoDao.getTotalTodosCount(projectId)
    
    fun getCompletedTodosCount(projectId: Int): Flow<Int> = projectTodoDao.getCompletedTodosCount(projectId)
    
    suspend fun insertTodo(todo: ProjectTodo): Long = projectTodoDao.insertTodo(todo)
    
    suspend fun insertTodos(todos: List<ProjectTodo>) = projectTodoDao.insertTodos(todos)
    
    suspend fun updateTodo(todo: ProjectTodo) = projectTodoDao.updateTodo(todo)
    
    suspend fun updateTodoCompletion(id: Int, isCompleted: Boolean, completedAt: LocalDateTime?) = 
        projectTodoDao.updateTodoCompletion(id, isCompleted, completedAt)
    
    suspend fun deleteTodo(todo: ProjectTodo) = projectTodoDao.deleteTodo(todo)
    
    suspend fun deleteTodosByProject(projectId: Int) = projectTodoDao.deleteTodosByProject(projectId)
}

