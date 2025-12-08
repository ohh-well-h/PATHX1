package com.example.pathx01.data.db

import android.content.Context
import com.example.pathx01.data.model.Book
import com.example.pathx01.data.model.BookStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).bookDao()

    suspend fun getAll(): List<Book> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.toModel() }
    }

    suspend fun insert(book: Book): Int = withContext(Dispatchers.IO) {
        dao.insert(book.toEntity()).toInt()
    }

    suspend fun update(book: Book) = withContext(Dispatchers.IO) {
        dao.update(book.toEntity())
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    private fun BookEntity.toModel(): Book = Book(
        id = id,
        title = title,
        author = author,
        status = BookStatus.valueOf(status),
        totalPages = totalPages,
        pagesRead = pagesRead,
        rating = rating,
        startedDate = startedDate,
        completedDate = completedDate
    )

    private fun Book.toEntity(): BookEntity = BookEntity(
        id = id,
        title = title,
        author = author,
        status = status.name,
        totalPages = totalPages ?: 0,
        pagesRead = pagesRead,
        rating = rating,
        startedDate = startedDate,
        completedDate = completedDate
    )
}


