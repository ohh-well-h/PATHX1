package com.example.pathx01.data.dao

import androidx.room.*
import com.example.pathx01.data.model.JournalEntry
import com.example.pathx01.data.model.Mood
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE date(date) = date(:date) ORDER BY date DESC")
    fun getEntriesByDate(date: LocalDateTime): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE mood = :mood ORDER BY date DESC")
    fun getEntriesByMood(mood: Mood): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE tags LIKE '%' || :tag || '%' ORDER BY date DESC")
    fun getEntriesByTag(tag: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Update
    suspend fun updateEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("SELECT DISTINCT tags FROM journal_entries WHERE tags IS NOT NULL AND tags != ''")
    fun getAllTags(): Flow<List<String>>

    @Query("SELECT * FROM journal_entries WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchEntries(query: String): Flow<List<JournalEntry>>
}
