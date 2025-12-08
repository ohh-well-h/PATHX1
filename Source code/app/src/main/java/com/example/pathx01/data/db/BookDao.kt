package com.example.pathx01.data.db

import androidx.room.*

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY id DESC")
    suspend fun getAll(): List<BookEntity>

    @Insert
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity): Int

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}


