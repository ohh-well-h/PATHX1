package com.example.pathx01.data.db

import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    suspend fun getAll(): List<TaskEntity>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}


