package com.example.pathx01.data.repository

import com.example.pathx01.data.dao.WritingEntryDao
import com.example.pathx01.data.model.WritingEntry
import com.example.pathx01.data.model.WritingType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class WritingEntryRepository constructor(
    private val writingEntryDao: WritingEntryDao
) {
    
    fun getEntriesByType(type: WritingType): Flow<List<WritingEntry>> = writingEntryDao.getEntriesByType(type)
    
    fun getAllEntries(): Flow<List<WritingEntry>> = writingEntryDao.getAllEntries()
    
    suspend fun getEntryById(id: Int): WritingEntry? = writingEntryDao.getEntryById(id)
    
    fun searchEntries(query: String): Flow<List<WritingEntry>> = writingEntryDao.searchEntries(query)
    
    fun getEntriesByTag(tag: String): Flow<List<WritingEntry>> = writingEntryDao.getEntriesByTag(tag)
    
    fun getEntriesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<WritingEntry>> = 
        writingEntryDao.getEntriesByDateRange(startDate, endDate)
    
    suspend fun insertEntry(entry: WritingEntry): Long = writingEntryDao.insertEntry(entry)
    
    suspend fun updateEntry(entry: WritingEntry) = writingEntryDao.updateEntry(entry)
    
    suspend fun deleteEntry(entry: WritingEntry) = writingEntryDao.deleteEntry(entry)
    
    suspend fun deleteEntriesByType(type: WritingType) = writingEntryDao.deleteEntriesByType(type)
}

