package com.example.pathx01.data.db

import androidx.room.*

@Dao
interface WritingEntryDao {
    @Query("SELECT * FROM writing_entries ORDER BY createdAt DESC")
    suspend fun getAll(): List<WritingEntryEntity>

    @Insert
    suspend fun insert(entry: WritingEntryEntity): Long

    @Update
    suspend fun update(entry: WritingEntryEntity): Int

    @Query("DELETE FROM writing_entries WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}


