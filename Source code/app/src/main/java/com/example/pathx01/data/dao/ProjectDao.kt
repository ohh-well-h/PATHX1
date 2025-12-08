package com.example.pathx01.data.dao

import androidx.room.*
import com.example.pathx01.data.model.Project
import com.example.pathx01.data.model.ProjectCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE isCompleted = 0 ORDER BY targetDate ASC")
    fun getActiveProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE isCompleted = 1 ORDER BY completedDate DESC")
    fun getCompletedProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE category = :category ORDER BY updatedAt DESC")
    fun getProjectsByCategory(category: ProjectCategory): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("UPDATE projects SET progressPercentage = :progress, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProjectProgress(id: Int, progress: Int, updatedAt: java.time.LocalDateTime)

    @Query("UPDATE projects SET isCompleted = :isCompleted, completedDate = :completedDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProjectCompletion(id: Int, isCompleted: Boolean, completedDate: java.time.LocalDateTime?, updatedAt: java.time.LocalDateTime)
}
