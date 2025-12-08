package com.example.pathx01.di

import android.content.Context
import com.example.pathx01.data.database.PathXDatabase
import com.example.pathx01.data.repository.PathXRepository

object AppModule {
    
    private var database: PathXDatabase? = null
    private var repository: PathXRepository? = null
    
    fun getDatabase(context: Context): PathXDatabase {
        if (database == null) {
            database = PathXDatabase.getDatabase(context)
        }
        return database!!
    }
    
    fun getRepository(context: Context): PathXRepository {
        if (repository == null) {
            val db = getDatabase(context)
            repository = PathXRepository(
                taskDao = db.taskDao(),
                projectDao = null,
                bookDao = null,
                journalDao = null
            )
        }
        return repository!!
    }
}



