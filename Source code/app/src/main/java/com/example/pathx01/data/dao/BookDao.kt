package com.example.pathx01.data.dao

import androidx.room.*
import com.example.pathx01.data.model.Book
import com.example.pathx01.data.model.BookStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY updatedAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = :status ORDER BY updatedAt DESC")
    fun getBooksByStatus(status: BookStatus): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'READING' ORDER BY startedDate DESC")
    fun getCurrentlyReadingBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY completedDate DESC")
    fun getCompletedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Int): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE books SET status = :status, startedDate = :startedDate, completedDate = :completedDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBookStatus(id: Int, status: BookStatus, startedDate: java.time.LocalDateTime?, completedDate: java.time.LocalDateTime?, updatedAt: java.time.LocalDateTime)

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<Book>>
}
