package com.example.pathx01.data.repository

import com.example.pathx01.data.dao.TaskCategoryDao
import com.example.pathx01.data.model.TaskCategory
import kotlinx.coroutines.flow.Flow

class TaskCategoryRepository constructor(
    private val taskCategoryDao: TaskCategoryDao
) {
    
    fun getAllCategories(): Flow<List<TaskCategory>> = taskCategoryDao.getAllCategories()
    
    fun getDefaultCategories(): Flow<List<TaskCategory>> = taskCategoryDao.getDefaultCategories()
    
    fun getCustomCategories(): Flow<List<TaskCategory>> = taskCategoryDao.getCustomCategories()
    
    suspend fun getCategoryById(id: Int): TaskCategory? = taskCategoryDao.getCategoryById(id)
    
    suspend fun insertCategory(category: TaskCategory): Long = taskCategoryDao.insertCategory(category)
    
    suspend fun insertCategories(categories: List<TaskCategory>) = taskCategoryDao.insertCategories(categories)
    
    suspend fun updateCategory(category: TaskCategory) = taskCategoryDao.updateCategory(category)
    
    suspend fun deleteCategory(category: TaskCategory) = taskCategoryDao.deleteCategory(category)
    
    suspend fun deleteAllCustomCategories() = taskCategoryDao.deleteAllCustomCategories()
}

