package com.example.pathx01.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.pathx01.data.dao.TaskDao
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.converters.PriorityConverters

@Database(
    entities = [Task::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(PriorityConverters::class)
abstract class PathXDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: PathXDatabase? = null

        fun getDatabase(context: Context): PathXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PathXDatabase::class.java,
                    "pathx_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
