package com.example.pathx01.data.dao

import androidx.room.*
import com.example.pathx01.data.model.TaskCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCategoryDao {
    
    @Query("SELECT * FROM task_categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<TaskCategory>>
    
    @Query("SELECT * FROM task_categories WHERE isDefault = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<TaskCategory>>
    
    @Query("SELECT * FROM task_categories WHERE isDefault = 0 ORDER BY name ASC")
    fun getCustomCategories(): Flow<List<TaskCategory>>
    
    @Query("SELECT * FROM task_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): TaskCategory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TaskCategory): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<TaskCategory>)
    
    @Update
    suspend fun updateCategory(category: TaskCategory)
    
    @Delete
    suspend fun deleteCategory(category: TaskCategory)
    
    @Query("DELETE FROM task_categories WHERE isDefault = 0")
    suspend fun deleteAllCustomCategories()
}
