package com.example.pathx01

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.pathx01.di.AppModule
import com.example.pathx01.data.SampleData

class PathXApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with sample data
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppModule.getDatabase(this@PathXApplication)
                val repository = AppModule.getRepository(this@PathXApplication)
                
                // Add sample tasks
                SampleData.getSampleTasks().forEach { task ->
                    repository.insertTask(task)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}



