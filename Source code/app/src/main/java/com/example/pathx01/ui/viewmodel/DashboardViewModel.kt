package com.example.pathx01.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.repository.PathXRepository
import kotlinx.coroutines.flow.Flow

class DashboardViewModel(
    private val repository: PathXRepository
) : ViewModel() {

    fun getTodaysTasks(): Flow<List<Task>> = repository.getAllTasks()
    fun getUpcomingTasks(): Flow<List<Task>> = repository.getIncompleteTasks()
    fun getAllTasks(): Flow<List<Task>> = repository.getAllTasks()
    fun getCompletedTasks(): Flow<List<Task>> = repository.getCompletedTasks()
}