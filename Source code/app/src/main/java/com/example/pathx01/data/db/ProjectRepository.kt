package com.example.pathx01.data.db

import android.content.Context
import com.example.pathx01.data.Project
import com.example.pathx01.data.ProjectTodo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProjectRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).projectDao()
    private val json = Json

    suspend fun getAll(): List<Project> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.toModel() }
    }

    suspend fun insert(project: Project): Int = withContext(Dispatchers.IO) {
        dao.insert(project.toEntity()).toInt()
    }

    suspend fun update(project: Project) = withContext(Dispatchers.IO) {
        dao.update(project.toEntity())
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    private fun ProjectEntity.toModel(): Project = Project(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        todos = runCatching { json.decodeFromString<List<ProjectTodo>>(todosJson) }.getOrDefault(emptyList())
    )

    private fun Project.toEntity(): ProjectEntity = ProjectEntity(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        todosJson = json.encodeToString(todos)
    )
}


