package com.example.pathx01.data.dao

import androidx.room.*
import com.example.pathx01.data.model.ProjectTodo
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectTodoDao {
    
    @Query("SELECT * FROM project_todos WHERE projectId = :projectId ORDER BY priority DESC, createdAt ASC")
    fun getTodosByProject(projectId: Int): Flow<List<ProjectTodo>>
    
    @Query("SELECT * FROM project_todos WHERE id = :id")
    suspend fun getTodoById(id: Int): ProjectTodo?
    
    @Query("SELECT COUNT(*) FROM project_todos WHERE projectId = :projectId")
    fun getTotalTodosCount(projectId: Int): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM project_todos WHERE projectId = :projectId AND isCompleted = 1")
    fun getCompletedTodosCount(projectId: Int): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: ProjectTodo): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<ProjectTodo>)
    
    @Update
    suspend fun updateTodo(todo: ProjectTodo)
    
    @Query("UPDATE project_todos SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTodoCompletion(id: Int, isCompleted: Boolean, completedAt: java.time.LocalDateTime?)
    
    @Delete
    suspend fun deleteTodo(todo: ProjectTodo)
    
    @Query("DELETE FROM project_todos WHERE projectId = :projectId")
    suspend fun deleteTodosByProject(projectId: Int)
}
