package com.example.pathx01.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.model.Priority
import com.example.pathx01.data.repository.PathXRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PlannerViewModel(
    private val repository: PathXRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())
    val filteredTasks: StateFlow<List<Task>> = _filteredTasks.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadTasks()
        loadCategories()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAllTasks().collect { taskList ->
                _tasks.value = taskList
                filterTasks()
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
    }

    private fun filterTasks() {
        val currentTasks = _tasks.value
        val currentCategory = _selectedCategory.value
        
        _filteredTasks.value = if (currentCategory == null) {
            currentTasks
        } else {
            currentTasks.filter { it.category == currentCategory }
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        filterTasks()
    }

    fun addTask(
        title: String,
        description: String?,
        category: String,
        priority: Priority,
        dueDate: LocalDateTime?
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDate = dueDate?.toString()
            )
            repository.insertTask(task)
        }
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskCompletion(taskId, isCompleted)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun getTasksByCategory(category: String): List<Task> {
        return _tasks.value.filter { it.category == category }
    }

    fun getIncompleteTasks(): List<Task> {
        return _tasks.value.filter { !it.isCompleted }
    }

    fun addCustomCategory(categoryName: String) {
        // Categories are automatically added when tasks with new categories are created
    }
}