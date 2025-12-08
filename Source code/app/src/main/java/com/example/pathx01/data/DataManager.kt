package com.example.pathx01.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.model.Priority
import java.time.LocalDateTime
import com.example.pathx01.data.model.Book
import com.example.pathx01.data.model.BookStatus
import com.example.pathx01.data.model.WritingEntry
import com.example.pathx01.data.model.WritingType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import android.util.Log
import com.example.pathx01.data.model.LocalDateTimeSerializer
import kotlinx.coroutines.*
import com.example.pathx01.data.db.WritingEntryRepository
import com.example.pathx01.data.db.TaskRepository
import com.example.pathx01.data.db.ProjectRepository
import com.example.pathx01.data.db.BookRepository

// Simple data manager to share data across screens with persistence
object DataManager {
    private var context: Context? = null
    private var prefs: SharedPreferences? = null
    private val json = Json { ignoreUnknownKeys = true }
    // Emits a pulse when a save completes; UI can react to show a Saved indicator
    val savePulse = mutableStateOf(0)
    
    // Auto-save mechanism
    private var autoSaveJob: Job? = null
    private val autoSaveScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var writingRepo: WritingEntryRepository? = null
    private var taskRepo: TaskRepository? = null
    private var projectRepo: ProjectRepository? = null
    private var bookRepo: BookRepository? = null
    
    fun initialize(context: Context) {
        try {
            this.context = context
            this.prefs = context.getSharedPreferences("pathx_data", Context.MODE_PRIVATE)
            Log.d("DataManager", "Initializing DataManager with context: ${context.packageName}")
            Log.d("DataManager", "SharedPreferences file: pathx_data")
            // Init Room repository
            writingRepo = com.example.pathx01.data.db.WritingEntryRepository(context)
            taskRepo = com.example.pathx01.data.db.TaskRepository(context)
            projectRepo = com.example.pathx01.data.db.ProjectRepository(context)
            bookRepo = com.example.pathx01.data.db.BookRepository(context)
            loadPersistedData()
            
            // Start auto-save mechanism - saves every 30 seconds
            startAutoSave()
        } catch (e: Exception) {
            Log.e("DataManager", "Error initializing DataManager", e)
            // Initialize with default data if there's an error
            initializeDefaultData()
        }
    }
    
    // Auto-save mechanism
    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = autoSaveScope.launch {
            while (isActive) {
                delay(30000) // Save every 30 seconds
                try {
                    Log.d("DataManager", "Auto-save triggered")
                    forceSaveAll()
                } catch (e: Exception) {
                    Log.e("DataManager", "Error in auto-save", e)
                }
            }
        }
        Log.d("DataManager", "Auto-save started")
    }
    
    fun stopAutoSave() {
        autoSaveJob?.cancel()
        Log.d("DataManager", "Auto-save stopped")
    }
    
    // Cleanup method
    fun cleanup() {
        stopAutoSave()
        autoSaveScope.cancel()
    }
    
    // Debug function to check current state
    fun debugPrintState() {
        Log.d("DataManager", "=== DataManager State ===")
        Log.d("DataManager", "Writing Entries: ${_writingEntries.size}")
        _writingEntries.forEachIndexed { index, entry ->
            Log.d("DataManager", "  [$index] ${entry.title} (${entry.type})")
        }
        Log.d("DataManager", "Books: ${_books.size}")
        _books.forEachIndexed { index, book ->
            Log.d("DataManager", "  [$index] ${book.title} by ${book.author}")
        }
        prefs?.let { prefs ->
            val writingJson = prefs.getString("writing_entries", null)
            val booksJson = prefs.getString("books", null)
            Log.d("DataManager", "Saved writing entries JSON: ${writingJson?.take(100) ?: "null"}")
            Log.d("DataManager", "Saved books JSON: ${booksJson?.take(100) ?: "null"}")
            Log.d("DataManager", "Is first launch: ${prefs.getBoolean("is_first_launch", true)}")
            
            // Check all keys in SharedPreferences
            val allKeys = prefs.all.keys
            Log.d("DataManager", "All SharedPreferences keys: $allKeys")
            allKeys.forEach { key ->
                val value = prefs.getString(key, null)
                Log.d("DataManager", "  $key: ${value?.take(50) ?: "null"}")
            }
        }
        Log.d("DataManager", "========================")
    }
    
    // Force save all data (for testing)
    fun forceSaveAll() {
        try {
            Log.d("DataManager", "Force saving all data...")
            Log.d("DataManager", "Current state - Writing: ${_writingEntries.size}, Books: ${_books.size}, Tasks: ${_tasks.size}, Projects: ${_projects.size}")
            saveWritingEntries()
            saveBooks()
            saveTasks()
            saveProjects()
            Log.d("DataManager", "Force save complete - Writing: ${_writingEntries.size}, Books: ${_books.size}, Tasks: ${_tasks.size}, Projects: ${_projects.size}")
        } catch (e: Exception) {
            Log.e("DataManager", "Error in forceSaveAll", e)
        }
    }
    
    // Immediate save for critical operations
    fun immediateSave() {
        try {
            Log.d("DataManager", "Immediate save triggered")
            saveWritingEntries()
            saveBooks()
            saveTasks()
            saveProjects()
            Log.d("DataManager", "Immediate save complete")
        } catch (e: Exception) {
            Log.e("DataManager", "Error in immediateSave", e)
        }
    }
    
    // Manual save and reload test
    fun manualSaveAndReloadTest() {
        try {
            Log.d("DataManager", "=== Manual Save and Reload Test ===")
            
            // Add a test entry
            val testEntry = WritingEntry(
                id = 886,
                title = "Manual Test Entry",
                content = "This entry was added manually for testing",
                type = WritingType.JOURNAL,
                mood = "Happy",
                tags = listOf("manual", "test"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now()
            )
            
            _writingEntries.add(testEntry)
            Log.d("DataManager", "Added manual test entry, total entries: ${_writingEntries.size}")
            
            // Force save
            saveWritingEntries()
            Log.d("DataManager", "Force saved writing entries")
            
            // Clear the in-memory list
            val originalSize = _writingEntries.size
            _writingEntries.clear()
            Log.d("DataManager", "Cleared in-memory list, size now: ${_writingEntries.size}")
            
            // Manually reload from SharedPreferences
            val writingEntriesJson = prefs?.getString("writing_entries", null)
            Log.d("DataManager", "Retrieved JSON from SharedPreferences: ${writingEntriesJson?.take(100)}")
            
            if (writingEntriesJson != null && writingEntriesJson != "[]") {
                val entries = json.decodeFromString<List<WritingEntry>>(writingEntriesJson)
                _writingEntries.clear()
                _writingEntries.addAll(entries)
                Log.d("DataManager", "Manually loaded ${entries.size} writing entries")
                
                if (_writingEntries.size == originalSize) {
                    Log.d("DataManager", "Manual test PASSED - data persisted correctly")
                } else {
                    Log.e("DataManager", "Manual test FAILED - size mismatch: ${_writingEntries.size} vs $originalSize")
                }
            } else {
                Log.e("DataManager", "Manual test FAILED - no data found in SharedPreferences")
            }
            
        } catch (e: Exception) {
            Log.e("DataManager", "Manual save and reload test failed", e)
        }
    }
    
    // Test function to manually add a test entry
    fun addTestEntry() {
        try {
            val testEntry = WritingEntry(
                id = 999,
                title = "Test Entry",
                content = "This is a test entry to verify persistence",
                type = WritingType.JOURNAL,
                mood = "Happy",
                tags = listOf("test"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now()
            )
            _writingEntries.add(testEntry)
            saveWritingEntries()
            Log.d("DataManager", "Added test entry, total entries: ${_writingEntries.size}")
        } catch (e: Exception) {
            Log.e("DataManager", "Error adding test entry", e)
        }
    }
    
    // Test function with string date instead of LocalDateTime
    fun addTestEntryWithStringDate() {
        try {
            // Create a simple map-based entry to test if LocalDateTime is the issue
            val simpleEntry = mapOf(
                "id" to 998,
                "title" to "Test Entry (String Date)",
                "content" to "Testing with string date instead of LocalDateTime",
                "type" to "JOURNAL",
                "mood" to "Happy",
                "tags" to listOf("test"),
                "createdAt" to System.currentTimeMillis().toString()
            )
            
            val jsonString = json.encodeToString(simpleEntry)
            prefs?.edit()?.putString("test_entry_string_date", jsonString)?.apply()
            Log.d("DataManager", "Saved test entry with string date: $jsonString")
            
            // Also add to the list for UI testing
            val testEntry = WritingEntry(
                id = 998,
                title = "Test Entry (String Date)",
                content = "Testing with string date instead of LocalDateTime",
                type = WritingType.JOURNAL,
                mood = "Happy",
                tags = listOf("test"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now()
            )
            _writingEntries.add(testEntry)
            saveWritingEntries()
            Log.d("DataManager", "Added test entry with string date, total entries: ${_writingEntries.size}")
        } catch (e: Exception) {
            Log.e("DataManager", "Error adding test entry with string date", e)
        }
    }
    
    // Test function to clear all data and reset first launch flag
    fun resetApp() {
        prefs?.edit()?.clear()?.apply()
        _writingEntries.clear()
        _books.clear()
        _tasks.clear()
        _projects.clear()
        Log.d("DataManager", "App reset - all data cleared")
    }
    
    // Test serialization directly
    fun testSerialization() {
        try {
            val testEntry = WritingEntry(
                id = 888,
                title = "Serialization Test",
                content = "Testing if serialization works",
                type = WritingType.JOURNAL,
                mood = "Happy",
                tags = listOf("test"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now()
            )
            
            val jsonString = json.encodeToString(testEntry)
            Log.d("DataManager", "Serialization test successful: $jsonString")
            
            val deserializedEntry = json.decodeFromString<WritingEntry>(jsonString)
            Log.d("DataManager", "Deserialization test successful: ${deserializedEntry.title}")
            
        } catch (e: Exception) {
            Log.e("DataManager", "Serialization test failed", e)
        }
    }
    
    // Test the exact same serialization as used in saveWritingEntries
    fun testExactSerialization() {
        try {
            Log.d("DataManager", "Testing exact serialization used in saveWritingEntries...")
            
            // Create a test entry
            val testEntry = WritingEntry(
                id = 887,
                title = "Exact Serialization Test",
                content = "Testing exact same serialization as saveWritingEntries",
                type = WritingType.JOURNAL,
                mood = "Happy",
                tags = listOf("test"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now()
            )
            
            // Test the exact same serialization as saveWritingEntries
            val testList = listOf(testEntry)
            val jsonString = json.encodeToString(testList)
            Log.d("DataManager", "Exact serialization successful: $jsonString")
            
            // Test the exact same deserialization as loadPersistedData
            val deserializedList = json.decodeFromString<List<WritingEntry>>(jsonString)
            Log.d("DataManager", "Exact deserialization successful: ${deserializedList.size} entries")
            Log.d("DataManager", "First entry title: ${deserializedList.first().title}")
            
            // Test saving to SharedPreferences
            prefs?.edit()?.putString("exact_test", jsonString)?.apply()
            val loadedJson = prefs?.getString("exact_test", null)
            
            if (jsonString == loadedJson) {
                Log.d("DataManager", "Exact test PASSED - SharedPreferences working")
            } else {
                Log.e("DataManager", "Exact test FAILED - SharedPreferences issue")
            }
            
        } catch (e: Exception) {
            Log.e("DataManager", "Exact serialization test failed", e)
        }
    }
    
    // Test simple SharedPreferences save/load
    fun testSimplePersistence() {
        try {
            // Test simple string save/load
            val testString = "Test data - ${System.currentTimeMillis()}"
            prefs?.edit()?.putString("test_key", testString)?.apply()
            Log.d("DataManager", "Saved test string: $testString")
            
            val loadedString = prefs?.getString("test_key", null)
            Log.d("DataManager", "Loaded test string: $loadedString")
            
            if (testString == loadedString) {
                Log.d("DataManager", "Simple persistence test PASSED")
            } else {
                Log.e("DataManager", "Simple persistence test FAILED")
            }
            
        } catch (e: Exception) {
            Log.e("DataManager", "Simple persistence test failed", e)
        }
    }
    
    // Test with simple data structure (no LocalDateTime)
    fun testSimpleWritingEntry() {
        try {
            val simpleEntry = mapOf(
                "id" to 777,
                "title" to "Simple Test Entry",
                "content" to "This is a simple test without LocalDateTime",
                "type" to "JOURNAL",
                "mood" to "Happy"
            )
            
            // Use kotlinx serialization instead of Gson
            val jsonString = json.encodeToString(simpleEntry)
            prefs?.edit()?.putString("simple_test_entry", jsonString)?.apply()
            Log.d("DataManager", "Saved simple entry: $jsonString")
            
            val loadedJson = prefs?.getString("simple_test_entry", null)
            Log.d("DataManager", "Loaded simple entry: $loadedJson")
            
            if (jsonString == loadedJson) {
                Log.d("DataManager", "Simple entry test PASSED")
            } else {
                Log.e("DataManager", "Simple entry test FAILED")
            }
            
        } catch (e: Exception) {
            Log.e("DataManager", "Simple entry test failed", e)
        }
    }
    // Shared task data
    private val _tasks = mutableStateListOf<Task>()
    
    // Shared project data
    private val _projects = mutableStateListOf<Project>()
    
    // Shared writing entries data
    private val _writingEntries = mutableStateListOf<com.example.pathx01.data.model.WritingEntry>()
    
    // Shared books data
    private val _books = mutableStateListOf<Book>()
    
    // Track which IDs are example data (for proper cleanup)
    private val exampleTaskIds = mutableSetOf<Int>()
    private val exampleProjectIds = mutableSetOf<Int>()
    private val exampleWritingEntryIds = mutableSetOf<Int>()
    private val exampleBookIds = mutableSetOf<Int>()
    
    // Don't initialize in init block - wait for initialize() to be called
    
    private fun initializeDefaultData() {
        // Clear existing example data tracking
        exampleTaskIds.clear()
        exampleProjectIds.clear()
        exampleWritingEntryIds.clear()
        exampleBookIds.clear()
        
        // Initialize books
        _books.clear()
        val exampleBooks = listOf(
            Book(
                id = 1,
                title = "Atomic Habits",
                author = "James Clear",
                status = BookStatus.READING,
                totalPages = 320,
                pagesRead = 120,
                rating = null,
                startedDate = LocalDateTime.now().minusDays(10),
                completedDate = null
            ),
            Book(
                id = 2,
                title = "The Alchemist",
                author = "Paulo Coelho",
                status = BookStatus.COMPLETED,
                totalPages = 163,
                pagesRead = 163,
                rating = 5,
                startedDate = LocalDateTime.now().minusDays(20),
                completedDate = LocalDateTime.now().minusDays(5)
            ),
            Book(
                id = 3,
                title = "Thinking, Fast and Slow",
                author = "Daniel Kahneman",
                status = BookStatus.TO_READ,
                totalPages = 499,
                pagesRead = 0,
                rating = null,
                startedDate = null,
                completedDate = null
            )
        )
        _books.addAll(exampleBooks)
        exampleBooks.forEach { exampleBookIds.add(it.id) }
        
        // Initialize tasks
        _tasks.clear()
        val exampleTasks = listOf(
            Task(
                id = 1,
                title = "Math Assignment",
                description = "Complete calculus problems",
                category = "Academic",
                priority = Priority.HIGH,
                dueDate = LocalDateTime.now().withHour(23).withMinute(59).toString(),
                subtasks = null,
                isCompleted = false
            ),
            Task(
                id = 2,
                title = "SAT Practice",
                description = "Take practice test",
                category = "Test Prep",
                priority = Priority.MEDIUM,
                dueDate = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).toString(),
                subtasks = null,
                isCompleted = false
            ),
            Task(
                id = 3,
                title = "College Applications",
                description = "Submit applications",
                category = "College Apps",
                priority = Priority.URGENT,
                dueDate = LocalDateTime.now().plusDays(3).withHour(12).withMinute(0).toString(),
                subtasks = null,
                isCompleted = true
            ),
            Task(
                id = 4,
                title = "Physics Lab Report",
                description = "Write lab report",
                category = "Academic",
                priority = Priority.MEDIUM,
                dueDate = LocalDateTime.now().minusDays(1).withHour(16).withMinute(0).toString(),
                subtasks = null,
                isCompleted = true
            ),
            Task(
                id = 5,
                title = "Gym Workout",
                description = "Upper body training",
                category = "Personal",
                priority = Priority.LOW,
                dueDate = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0).toString(),
                subtasks = null,
                isCompleted = false
            ),
            Task(
                id = 6,
                title = "Chemistry Quiz",
                description = "Study for chemistry quiz",
                category = "Academic",
                priority = Priority.HIGH,
                dueDate = LocalDateTime.now().plusDays(4).withHour(10).withMinute(0).toString(),
                subtasks = null,
                isCompleted = false
            )
        )
        _tasks.addAll(exampleTasks)
        // Track these as example data
        exampleTasks.forEach { exampleTaskIds.add(it.id) }
        
        // Initialize projects
        _projects.clear()
        val exampleProjects = listOf(
            Project(
                id = 1,
                title = "Science Fair Project",
                description = "Develop a sustainable energy solution",
                createdAt = LocalDateTime.now().minusDays(5),
                todos = listOf(
                    ProjectTodo("Research renewable energy sources", true),
                    ProjectTodo("Design prototype", false),
                    ProjectTodo("Build prototype", false),
                    ProjectTodo("Test and document results", false)
                )
            ),
            Project(
                id = 2,
                title = "Personal Website",
                description = "Create a portfolio website",
                createdAt = LocalDateTime.now().minusDays(3),
                todos = listOf(
                    ProjectTodo("Design wireframes", true),
                    ProjectTodo("Set up development environment", true),
                    ProjectTodo("Create homepage", false),
                    ProjectTodo("Add portfolio section", false),
                    ProjectTodo("Deploy to web", false)
                )
            )
        )
        _projects.addAll(exampleProjects)
        // Track these as example data
        exampleProjects.forEach { exampleProjectIds.add(it.id) }
        
        // Initialize writing entries
        _writingEntries.clear()
        val exampleWritingEntries = listOf(
            com.example.pathx01.data.model.WritingEntry(
                id = 1,
                title = "Daily Reflection",
                content = "Today was a productive day. I completed my math assignment and made progress on my science project.",
                type = com.example.pathx01.data.model.WritingType.JOURNAL,
                mood = "Happy",
                tags = listOf("reflection", "productivity"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now().minusDays(1)
            ),
            com.example.pathx01.data.model.WritingEntry(
                id = 2,
                title = "Study Notes",
                content = "Key concepts from today's chemistry lecture:\n- Chemical bonding\n- Molecular structures\n- Reaction mechanisms",
                type = com.example.pathx01.data.model.WritingType.NOTE,
                mood = null,
                tags = listOf("chemistry", "study", "notes"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now().minusHours(2)
            )
        )
        _writingEntries.addAll(exampleWritingEntries)
        // Track these as example data
        exampleWritingEntries.forEach { exampleWritingEntryIds.add(it.id) }
    }
    
    // Task operations
    fun addTask(task: Task) {
        runBlocking(Dispatchers.IO) { taskRepo?.insert(task) }
        refreshTasksFromDb()
        saveTasks()
        Log.d("DataManager", "Added task: ${task.title}")
    }
    
    fun updateTask(updatedTask: Task) {
        runBlocking(Dispatchers.IO) { taskRepo?.update(updatedTask) }
        refreshTasksFromDb()
        saveTasks()
        Log.d("DataManager", "Updated task: ${updatedTask.title}, ID: ${updatedTask.id}")
    }
    
    fun deleteTask(taskId: Int) {
        runBlocking(Dispatchers.IO) { taskRepo?.delete(taskId) }
        refreshTasksFromDb()
        saveTasks()
        Log.d("DataManager", "Deleted task ID: $taskId")
    }
    
    fun toggleTaskCompletion(taskId: Int) {
        val task = _tasks.find { it.id == taskId } ?: return
        val updated = task.copy(isCompleted = !task.isCompleted)
        runBlocking(Dispatchers.IO) { taskRepo?.update(updated) }
        refreshTasksFromDb()
        saveTasks()
        Log.d("DataManager", "Toggled task completion: ${updated.title}, ID: $taskId, Completed: ${updated.isCompleted}")
    }
    
    fun updateTaskSubtasks(taskId: Int, subtasks: List<com.example.pathx01.data.model.Subtask>) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val subtasksJson = com.example.pathx01.utils.SubtaskUtils.subtasksToJson(subtasks)
            _tasks[index] = _tasks[index].copy(subtasks = subtasksJson)
            saveTasks()
        }
    }
    
    fun getTaskSubtasks(taskId: Int): List<com.example.pathx01.data.model.Subtask> {
        val task = _tasks.find { it.id == taskId }
        return task?.let { com.example.pathx01.utils.SubtaskUtils.jsonToSubtasks(it.subtasks) } ?: emptyList()
    }
    
    // Project operations
    fun addProject(project: Project) {
        runBlocking(Dispatchers.IO) { projectRepo?.insert(project) }
        refreshProjectsFromDb()
        saveProjects()
        Log.d("DataManager", "Added project: ${project.title}")
    }
    
    fun updateProject(updatedProject: Project) {
        runBlocking(Dispatchers.IO) { projectRepo?.update(updatedProject) }
        refreshProjectsFromDb()
        saveProjects()
        Log.d("DataManager", "Updated project: ${updatedProject.title}, ID: ${updatedProject.id}")
    }
    
    fun deleteProject(projectId: Int) {
        runBlocking(Dispatchers.IO) { projectRepo?.delete(projectId) }
        refreshProjectsFromDb()
        saveProjects()
        Log.d("DataManager", "Deleted project ID: $projectId")
    }
    
    // Writing entries operations
    fun addWritingEntry(entry: com.example.pathx01.data.model.WritingEntry) {
        // Persist first, then refresh list
        runBlocking(Dispatchers.IO) {
            val id = writingRepo?.insert(entry) ?: 0
            Log.d("DataManager", "DB insert writing entry id=$id")
        }
        refreshWritingEntriesFromDb()
        saveWritingEntries()
        Log.d("DataManager", "Added writing entry: ${entry.title}")
    }
    
    fun updateWritingEntry(updatedEntry: com.example.pathx01.data.model.WritingEntry) {
        runBlocking(Dispatchers.IO) {
            writingRepo?.update(updatedEntry)
        }
        refreshWritingEntriesFromDb()
        saveWritingEntries()
        Log.d("DataManager", "Updated writing entry: ${updatedEntry.title}, ID: ${updatedEntry.id}")
    }
    
    fun deleteWritingEntry(entryId: Int) {
        runBlocking(Dispatchers.IO) {
            writingRepo?.delete(entryId)
        }
        refreshWritingEntriesFromDb()
        saveWritingEntries()
        Log.d("DataManager", "Deleted writing entry ID: $entryId")
    }
    
    // Book management methods
    fun addBook(book: Book) {
        runBlocking(Dispatchers.IO) { bookRepo?.insert(book) }
        refreshBooksFromDb()
        saveBooks()
        Log.d("DataManager", "Added book: ${book.title}")
    }
    
    fun updateBook(updatedBook: Book) {
        runBlocking(Dispatchers.IO) { bookRepo?.update(updatedBook) }
        refreshBooksFromDb()
        saveBooks()
        Log.d("DataManager", "Updated book: ${updatedBook.title}")
    }
    
    fun deleteBook(bookId: Int) {
        runBlocking(Dispatchers.IO) { bookRepo?.delete(bookId) }
        refreshBooksFromDb()
        saveBooks()
        Log.d("DataManager", "Deleted book ID: $bookId")
    }
    
    fun getNextBookId(): Int {
        return (_books.maxOfOrNull { it.id } ?: 0) + 1
    }
    
    // Example data management
    fun clearAllData() {
        // Save IDs before clearing (needed for database deletion)
        val taskIdsToDelete = _tasks.map { it.id }
        val projectIdsToDelete = _projects.map { it.id }
        val writingIdsToDelete = _writingEntries.map { it.id }
        val bookIdsToDelete = _books.map { it.id }
        
        _tasks.clear()
        _projects.clear()
        _writingEntries.clear()
        _books.clear()
        exampleTaskIds.clear()
        exampleProjectIds.clear()
        exampleWritingEntryIds.clear()
        exampleBookIds.clear()
        
        // Persist the cleared state to disk
        immediateSave()
        
        // Also clear database by deleting all items
        runBlocking(Dispatchers.IO) {
            taskIdsToDelete.forEach { taskRepo?.delete(it) }
            projectIdsToDelete.forEach { projectRepo?.delete(it) }
            writingIdsToDelete.forEach { writingRepo?.delete(it) }
            bookIdsToDelete.forEach { bookRepo?.delete(it) }
        }
    }
    
    fun loadExampleData() {
        // Don't clear all data - just add example data with unique IDs
        // First, find the highest existing IDs to avoid conflicts
        val maxTaskId = _tasks.maxOfOrNull { it.id } ?: 0
        val maxProjectId = _projects.maxOfOrNull { it.id } ?: 0
        val maxWritingEntryId = _writingEntries.maxOfOrNull { it.id } ?: 0
        val maxBookId = _books.maxOfOrNull { it.id } ?: 0
        
        // Create example data with IDs that won't conflict
        val exampleTaskIdStart = maxTaskId + 1000 // Use 1000+ range for example data
        val exampleProjectIdStart = maxProjectId + 1000
        val exampleWritingEntryIdStart = maxWritingEntryId + 1000
        val exampleBookIdStart = maxBookId + 1000
        
        // Add example tasks with unique IDs
        val exampleTasks = listOf(
            Task(
                id = exampleTaskIdStart + 1,
                title = "Math Assignment",
                description = "Complete calculus problems",
                category = "Academic",
                priority = Priority.HIGH,
                dueDate = LocalDateTime.now().withHour(23).withMinute(59).toString(),
                subtasks = null,
                isCompleted = false
            ),
            Task(
                id = exampleTaskIdStart + 2,
                title = "SAT Practice",
                description = "Take practice test",
                category = "Test Prep",
                priority = Priority.MEDIUM,
                dueDate = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).toString(),
                subtasks = null,
                isCompleted = false
            ),
            Task(
                id = exampleTaskIdStart + 3,
                title = "College Applications",
                description = "Submit applications",
                category = "College Apps",
                priority = Priority.URGENT,
                dueDate = LocalDateTime.now().plusDays(3).withHour(12).withMinute(0).toString(),
                subtasks = null,
                isCompleted = true
            ),
            Task(
                id = exampleTaskIdStart + 4,
                title = "Physics Lab Report",
                description = "Write lab report",
                category = "Academic",
                priority = Priority.MEDIUM,
                dueDate = LocalDateTime.now().minusDays(1).withHour(16).withMinute(0).toString(),
                subtasks = null,
                isCompleted = true
            ),
            Task(
                id = exampleTaskIdStart + 5,
                title = "Gym Workout",
                description = "Upper body training",
                category = "Personal",
                priority = Priority.LOW,
                dueDate = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0).toString(),
                subtasks = null,
                isCompleted = false
            ),
            Task(
                id = exampleTaskIdStart + 6,
                title = "Chemistry Quiz",
                description = "Study for chemistry quiz",
                category = "Academic",
                priority = Priority.HIGH,
                dueDate = LocalDateTime.now().plusDays(4).withHour(10).withMinute(0).toString(),
                subtasks = null,
                isCompleted = false
            )
        )
        _tasks.addAll(exampleTasks)
        exampleTasks.forEach { exampleTaskIds.add(it.id) }
        
        // Add example projects with unique IDs
        val exampleProjects = listOf(
            Project(
                id = exampleProjectIdStart + 1,
                title = "Science Fair Project",
                description = "Develop a sustainable energy solution",
                createdAt = LocalDateTime.now().minusDays(5),
                todos = listOf(
                    ProjectTodo("Research renewable energy sources", true),
                    ProjectTodo("Design prototype", false),
                    ProjectTodo("Build prototype", false),
                    ProjectTodo("Test and document results", false)
                )
            ),
            Project(
                id = exampleProjectIdStart + 2,
                title = "Personal Website",
                description = "Create a portfolio website",
                createdAt = LocalDateTime.now().minusDays(3),
                todos = listOf(
                    ProjectTodo("Design wireframes", true),
                    ProjectTodo("Set up development environment", true),
                    ProjectTodo("Create homepage", false),
                    ProjectTodo("Add portfolio section", false),
                    ProjectTodo("Deploy to web", false)
                )
            )
        )
        _projects.addAll(exampleProjects)
        exampleProjects.forEach { exampleProjectIds.add(it.id) }
        
        // Add example writing entries with unique IDs
        val exampleWritingEntries = listOf(
            com.example.pathx01.data.model.WritingEntry(
                id = exampleWritingEntryIdStart + 1,
                title = "Daily Reflection",
                content = "Today was a productive day. I completed my math assignment and made progress on my science project.",
                type = com.example.pathx01.data.model.WritingType.JOURNAL,
                mood = "Happy",
                tags = listOf("reflection", "productivity"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now().minusDays(1)
            ),
            com.example.pathx01.data.model.WritingEntry(
                id = exampleWritingEntryIdStart + 2,
                title = "Study Notes",
                content = "Key concepts from today's chemistry lecture:\n- Chemical bonding\n- Molecular structures\n- Reaction mechanisms",
                type = com.example.pathx01.data.model.WritingType.NOTE,
                mood = null,
                tags = listOf("chemistry", "study", "notes"),
                attachments = emptyList(),
                checklists = emptyList(),
                createdAt = LocalDateTime.now().minusHours(2)
            )
        )
        _writingEntries.addAll(exampleWritingEntries)
        exampleWritingEntries.forEach { exampleWritingEntryIds.add(it.id) }
        
        // Add example books with unique IDs
        val exampleBooks = listOf(
            Book(
                id = exampleBookIdStart + 1,
                title = "Atomic Habits",
                author = "James Clear",
                status = BookStatus.READING,
                totalPages = 320,
                pagesRead = 120,
                rating = null,
                startedDate = LocalDateTime.now().minusDays(10),
                completedDate = null
            ),
            Book(
                id = exampleBookIdStart + 2,
                title = "The Alchemist",
                author = "Paulo Coelho",
                status = BookStatus.COMPLETED,
                totalPages = 163,
                pagesRead = 163,
                rating = 5,
                startedDate = LocalDateTime.now().minusDays(20),
                completedDate = LocalDateTime.now().minusDays(5)
            ),
            Book(
                id = exampleBookIdStart + 3,
                title = "Thinking, Fast and Slow",
                author = "Daniel Kahneman",
                status = BookStatus.TO_READ,
                totalPages = 499,
                pagesRead = 0,
                rating = null,
                startedDate = null,
                completedDate = null
            )
        )
        _books.addAll(exampleBooks)
        exampleBooks.forEach { exampleBookIds.add(it.id) }
        
        // Persist the example data to disk
        immediateSave()
        
        // Also save to database
        runBlocking(Dispatchers.IO) {
            exampleWritingEntries.forEach { writingRepo?.insert(it) }
            exampleBooks.forEach { bookRepo?.insert(it) }
            exampleTasks.forEach { taskRepo?.insert(it) }
            exampleProjects.forEach { projectRepo?.insert(it) }
        }
    }
    
    fun hasData(): Boolean {
        return _tasks.isNotEmpty() || _projects.isNotEmpty() || _writingEntries.isNotEmpty() || _books.isNotEmpty()
    }
    
    fun clearExampleData() {
        // Save copies of IDs before clearing (needed for database deletion)
        val idsToDeleteWriting = exampleWritingEntryIds.toList()
        val idsToDeleteBooks = exampleBookIds.toList()
        val idsToDeleteTasks = exampleTaskIds.toList()
        val idsToDeleteProjects = exampleProjectIds.toList()
        
        // Clear only the example data by removing items with tracked example IDs
        _tasks.removeAll { task -> exampleTaskIds.contains(task.id) }
        _projects.removeAll { project -> exampleProjectIds.contains(project.id) }
        _writingEntries.removeAll { entry -> exampleWritingEntryIds.contains(entry.id) }
        _books.removeAll { book -> exampleBookIds.contains(book.id) }
        
        // Clear the tracking sets since example data is gone
        exampleTaskIds.clear()
        exampleProjectIds.clear()
        exampleWritingEntryIds.clear()
        exampleBookIds.clear()
        
        // Persist the updated data to disk (all data types)
        immediateSave()
        
        // Also delete from database using saved IDs
        runBlocking(Dispatchers.IO) {
            idsToDeleteWriting.forEach { writingRepo?.delete(it) }
            idsToDeleteBooks.forEach { bookRepo?.delete(it) }
            idsToDeleteTasks.forEach { taskRepo?.delete(it) }
            idsToDeleteProjects.forEach { projectRepo?.delete(it) }
        }
    }
    
    // Persistence methods
    private fun loadPersistedData() {
        try {
            Log.d("DataManager", "Loading persisted data...")
            prefs?.let { prefs ->
                try {
                    // Check if this is the first launch
                    val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
                    Log.d("DataManager", "Is first launch: $isFirstLaunch")
                    
                    if (isFirstLaunch) {
                        // First launch - load example data
                        Log.d("DataManager", "First launch - loading example data")
                        initializeDefaultData()
                        // Save the example data
                        saveWritingEntries()
                        saveBooks()
                        saveTasks()
                        saveProjects()
                        prefs.edit().putBoolean("is_first_launch", false).commit()
                        Log.d("DataManager", "First launch flag set to false")
                    } else {
                        // Not first launch - load saved data
                        Log.d("DataManager", "Not first launch - loading saved data")
                        Log.d("DataManager", "Checking for saved data...")
                        val writingEntriesJson = prefs.getString("writing_entries", null)
                        val booksJson = prefs.getString("books", null)
                        val tasksJson = prefs.getString("tasks", null)
                        val projectsJson = prefs.getString("projects", null)
                        Log.d("DataManager", "Raw JSON - Writing: ${writingEntriesJson?.take(50)}, Books: ${booksJson?.take(50)}, Tasks: ${tasksJson?.take(50)}, Projects: ${projectsJson?.take(50)}")
                        
                        // Prefer DB writing entries; fallback to SharedPreferences
                        if (!refreshWritingEntriesFromDb()) {
                            if (writingEntriesJson != null && writingEntriesJson != "[]") {
                                try {
                                    val entries = json.decodeFromString<List<WritingEntry>>(writingEntriesJson)
                                    _writingEntries.clear()
                                    _writingEntries.addAll(entries)
                                    Log.d("DataManager", "Loaded ${entries.size} writing entries (prefs)")
                                } catch (e: Exception) {
                                    Log.e("DataManager", "Error parsing writing entries", e)
                                    _writingEntries.clear()
                                }
                            } else {
                                Log.d("DataManager", "No saved writing entries found")
                            }
                        }
                        
                        // Books from DB preferred
                        if (!refreshBooksFromDb()) {
                            if (booksJson != null && booksJson != "[]") {
                                try {
                                    val books = json.decodeFromString<List<Book>>(booksJson)
                                    _books.clear()
                                    _books.addAll(books)
                                    Log.d("DataManager", "Loaded ${books.size} books (prefs)")
                                } catch (e: Exception) {
                                    Log.e("DataManager", "Error parsing books", e)
                                    _books.clear()
                                }
                            } else {
                                Log.d("DataManager", "No saved books found")
                            }
                        }
                        
                        // Tasks from DB preferred
                        if (!refreshTasksFromDb()) {
                            if (tasksJson != null && tasksJson != "[]") {
                                try {
                                    val tasks = json.decodeFromString<List<Task>>(tasksJson)
                                    _tasks.clear()
                                    _tasks.addAll(tasks)
                                    Log.d("DataManager", "Loaded ${tasks.size} tasks (prefs)")
                                } catch (e: Exception) {
                                    Log.e("DataManager", "Error parsing tasks", e)
                                    _tasks.clear()
                                }
                            } else {
                                Log.d("DataManager", "No saved tasks found")
                            }
                        }
                        
                        // Projects from DB preferred
                        if (!refreshProjectsFromDb()) {
                            if (projectsJson != null && projectsJson != "[]") {
                                try {
                                    val projects = json.decodeFromString<List<Project>>(projectsJson)
                                    _projects.clear()
                                    _projects.addAll(projects)
                                    Log.d("DataManager", "Loaded ${projects.size} projects (prefs)")
                                } catch (e: Exception) {
                                    Log.e("DataManager", "Error parsing projects", e)
                                    _projects.clear()
                                }
                            } else {
                                Log.d("DataManager", "No saved projects found")
                            }
                        }
                        
                        // Log what was loaded
                        Log.d("DataManager", "Loaded data: Writing: ${_writingEntries.size}, Books: ${_books.size}, Tasks: ${_tasks.size}, Projects: ${_projects.size}")
                        
                        // Don't reset to first launch if user has data - they might have just cleared it intentionally
                        // Only initialize default data if this is truly the first time the app is running
                    }
                } catch (e: Exception) {
                    Log.e("DataManager", "Error loading persisted data", e)
                    initializeDefaultData()
                }
            } ?: run {
                Log.e("DataManager", "Prefs is null - initializing with default data")
                initializeDefaultData()
            }
        } catch (e: Exception) {
            Log.e("DataManager", "Critical error in loadPersistedData", e)
            // Fallback to default data
            initializeDefaultData()
        }
    }

    private fun refreshWritingEntriesFromDb(): Boolean {
        return try {
            val repo = writingRepo ?: return false
            val list = runBlocking(Dispatchers.IO) { repo.getAll() }
            _writingEntries.clear()
            _writingEntries.addAll(list)
            Log.d("DataManager", "Loaded ${list.size} writing entries (db)")
            true
        } catch (e: Exception) {
            Log.e("DataManager", "DB refresh failed", e)
            false
        }
    }

    private fun refreshTasksFromDb(): Boolean {
        return try {
            val repo = taskRepo ?: return false
            val list = runBlocking(Dispatchers.IO) { repo.getAll() }
            _tasks.clear()
            _tasks.addAll(list)
            Log.d("DataManager", "Loaded ${list.size} tasks (db)")
            true
        } catch (e: Exception) {
            Log.e("DataManager", "DB tasks refresh failed", e)
            false
        }
    }

    private fun refreshProjectsFromDb(): Boolean {
        return try {
            val repo = projectRepo ?: return false
            val list = runBlocking(Dispatchers.IO) { repo.getAll() }
            _projects.clear()
            _projects.addAll(list)
            Log.d("DataManager", "Loaded ${list.size} projects (db)")
            true
        } catch (e: Exception) {
            Log.e("DataManager", "DB projects refresh failed", e)
            false
        }
    }

    private fun refreshBooksFromDb(): Boolean {
        return try {
            val repo = bookRepo ?: return false
            val list = runBlocking(Dispatchers.IO) { repo.getAll() }
            _books.clear()
            _books.addAll(list)
            Log.d("DataManager", "Loaded ${list.size} books (db)")
            true
        } catch (e: Exception) {
            Log.e("DataManager", "DB books refresh failed", e)
            false
        }
    }

    // Public API to reload persisted data on demand (e.g., from a dialog)
    fun reloadFromDisk() {
        loadPersistedData()
    }

    // Load history directly from SharedPreferences without first-launch logic
    fun loadHistory(): Boolean {
        return try {
            val p = prefs ?: return false
            val writingEntriesJson = p.getString("writing_entries", null)
            val booksJson = p.getString("books", null)
            val tasksJson = p.getString("tasks", null)
            val projectsJson = p.getString("projects", null)

            var loadedAny = false

            // Prefer DB for writing entries
            val dbOk = refreshWritingEntriesFromDb()
            loadedAny = loadedAny || dbOk

            if (!writingEntriesJson.isNullOrBlank() && writingEntriesJson != "[]") {
                try {
                    val entries = json.decodeFromString<List<WritingEntry>>(writingEntriesJson)
                    _writingEntries.clear()
                    _writingEntries.addAll(entries)
                    loadedAny = loadedAny || entries.isNotEmpty()
                } catch (e: Exception) {
                    Log.e("DataManager", "History load: writing entries parse error", e)
                }
            }

            // Prefer DB for tasks/projects/books as well
            loadedAny = refreshTasksFromDb() || loadedAny
            loadedAny = refreshProjectsFromDb() || loadedAny
            loadedAny = refreshBooksFromDb() || loadedAny

            if (!booksJson.isNullOrBlank() && booksJson != "[]") {
                try {
                    val books = json.decodeFromString<List<Book>>(booksJson)
                    _books.clear()
                    _books.addAll(books)
                    loadedAny = loadedAny || books.isNotEmpty()
                } catch (e: Exception) {
                    Log.e("DataManager", "History load: books parse error", e)
                }
            }

            if (!tasksJson.isNullOrBlank() && tasksJson != "[]") {
                try {
                    val tasks = json.decodeFromString<List<Task>>(tasksJson)
                    _tasks.clear()
                    _tasks.addAll(tasks)
                    loadedAny = loadedAny || tasks.isNotEmpty()
                } catch (e: Exception) {
                    Log.e("DataManager", "History load: tasks parse error", e)
                }
            }

            if (!projectsJson.isNullOrBlank() && projectsJson != "[]") {
                try {
                    val projects = json.decodeFromString<List<Project>>(projectsJson)
                    _projects.clear()
                    _projects.addAll(projects)
                    loadedAny = loadedAny || projects.isNotEmpty()
                } catch (e: Exception) {
                    Log.e("DataManager", "History load: projects parse error", e)
                }
            }

            Log.d("DataManager", "History loaded: Writing=${_writingEntries.size}, Books=${_books.size}, Tasks=${_tasks.size}, Projects=${_projects.size}")
            loadedAny
        } catch (e: Exception) {
            Log.e("DataManager", "History load failed", e)
            false
        }
    }
    
    private fun saveWritingEntries() {
        try {
            val jsonString = json.encodeToString(_writingEntries)
            val result = prefs?.edit()?.putString("writing_entries", jsonString)?.commit()
            Log.d("DataManager", "Saved ${_writingEntries.size} writing entries, commit result: $result, JSON length: ${jsonString.length}")
            if (result == true) notifySaved()
        } catch (e: Exception) {
            Log.e("DataManager", "Error saving writing entries", e)
        }
    }
    
    private fun saveBooks() {
        try {
            val jsonString = json.encodeToString(_books)
            val result = prefs?.edit()?.putString("books", jsonString)?.commit()
            Log.d("DataManager", "Saved ${_books.size} books, commit result: $result, JSON length: ${jsonString.length}")
            if (result == true) notifySaved()
        } catch (e: Exception) {
            Log.e("DataManager", "Error saving books", e)
        }
    }
    
    private fun saveTasks() {
        try {
            val jsonString = json.encodeToString(_tasks)
            val result = prefs?.edit()?.putString("tasks", jsonString)?.commit()
            Log.d("DataManager", "Saved ${_tasks.size} tasks, commit result: $result, JSON length: ${jsonString.length}")
            if (result == true) notifySaved()
        } catch (e: Exception) {
            Log.e("DataManager", "Error saving tasks", e)
        }
    }
    
    private fun saveProjects() {
        try {
            val jsonString = json.encodeToString(_projects)
            val result = prefs?.edit()?.putString("projects", jsonString)?.commit()
            Log.d("DataManager", "Saved ${_projects.size} projects, commit result: $result, JSON length: ${jsonString.length}")
            if (result == true) notifySaved()
        } catch (e: Exception) {
            Log.e("DataManager", "Error saving projects", e)
        }
    }

    private fun notifySaved() {
        // Increment pulse so Compose observers can react
        savePulse.value = savePulse.value + 1
    }
    
    // Getters for reactive updates
    fun getTasks(): List<Task> = _tasks.toList()
    fun getProjects(): List<Project> = _projects.toList()
    
    // Return the actual observable list for Compose to observe
    fun getWritingEntriesObservable(): androidx.compose.runtime.snapshots.SnapshotStateList<WritingEntry> = _writingEntries
    fun getBooksObservable(): androidx.compose.runtime.snapshots.SnapshotStateList<Book> = _books
    
    // Keep these for backward compatibility
    fun getWritingEntries(): List<com.example.pathx01.data.model.WritingEntry> = _writingEntries.toList()
    fun getBooks(): List<Book> = _books.toList()
}

@Serializable
data class Project(
    val id: Int,
    val title: String,
    val description: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    val todos: List<ProjectTodo>
)

@Serializable
data class ProjectTodo(
    val text: String,
    val isCompleted: Boolean
)


