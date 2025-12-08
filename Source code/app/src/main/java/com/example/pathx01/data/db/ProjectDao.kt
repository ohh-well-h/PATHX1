package com.example.pathx01.data.db

import androidx.room.*

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    suspend fun getAll(): List<ProjectEntity>

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity): Int

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}


