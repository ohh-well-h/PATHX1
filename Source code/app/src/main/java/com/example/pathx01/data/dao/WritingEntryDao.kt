package com.example.pathx01.data.dao

import androidx.room.*
import com.example.pathx01.data.model.WritingEntry
import com.example.pathx01.data.model.WritingType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WritingEntryDao {
    
    @Query("SELECT * FROM writing_entries WHERE type = :type ORDER BY date DESC")
    fun getEntriesByType(type: WritingType): Flow<List<WritingEntry>>
    
    @Query("SELECT * FROM writing_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WritingEntry>>
    
    @Query("SELECT * FROM writing_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): WritingEntry?
    
    @Query("SELECT * FROM writing_entries WHERE title LIKE :query OR content LIKE :query ORDER BY date DESC")
    fun searchEntries(query: String): Flow<List<WritingEntry>>
    
    @Query("SELECT * FROM writing_entries WHERE tags LIKE :tag ORDER BY date DESC")
    fun getEntriesByTag(tag: String): Flow<List<WritingEntry>>
    
    @Query("SELECT * FROM writing_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getEntriesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<WritingEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WritingEntry): Long
    
    @Update
    suspend fun updateEntry(entry: WritingEntry)
    
    @Delete
    suspend fun deleteEntry(entry: WritingEntry)
    
    @Query("DELETE FROM writing_entries WHERE type = :type")
    suspend fun deleteEntriesByType(type: WritingType)
}

