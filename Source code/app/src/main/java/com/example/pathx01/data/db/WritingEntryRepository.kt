package com.example.pathx01.data.db

import android.content.Context
import com.example.pathx01.data.model.WritingEntry
import com.example.pathx01.data.model.WritingType
import com.example.pathx01.data.model.Attachment
import com.example.pathx01.data.model.ChecklistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WritingEntryRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.writingEntryDao()
    private val json = Json

    suspend fun getAll(): List<WritingEntry> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.toModel() }
    }

    suspend fun insert(entry: WritingEntry): Int = withContext(Dispatchers.IO) {
        val id = dao.insert(entry.toEntity()).toInt()
        id
    }

    suspend fun update(entry: WritingEntry) = withContext(Dispatchers.IO) {
        dao.update(entry.toEntity())
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    private fun WritingEntry.toEntity(): WritingEntryEntity = WritingEntryEntity(
        id = this.id,
        title = this.title,
        content = this.content,
        type = this.type.name,
        mood = this.mood,
        tagsJson = json.encodeToString(this.tags),
        attachmentsJson = json.encodeToString(this.attachments),
        checklistsJson = json.encodeToString(this.checklists),
        createdAt = this.createdAt
    )

    private fun WritingEntryEntity.toModel(): WritingEntry = WritingEntry(
        id = this.id,
        title = this.title,
        content = this.content,
        type = WritingType.valueOf(this.type),
        mood = this.mood,
        tags = this.tagsJson?.let { runCatching { json.decodeFromString<List<String>>(it) }.getOrNull() } ?: emptyList(),
        attachments = this.attachmentsJson?.let { runCatching { json.decodeFromString<List<Attachment>>(it) }.getOrNull() } ?: emptyList(),
        checklists = this.checklistsJson?.let { runCatching { json.decodeFromString<List<ChecklistItem>>(it) }.getOrNull() } ?: emptyList(),
        createdAt = this.createdAt
    )
}


