# Criterion C – Development
## PathX Android Application

---

## 1. Overview of Development

The PathX Android application was developed using **Android Studio Hedgehog** with **Kotlin 1.9** as the primary programming language. The development environment utilized modern Android development practices including **Jetpack Compose** for declarative UI, **Material Design 3** for consistent theming, and **Room Database v2.6** for persistent data storage.

**Key Dependencies:**
- **Jetpack Compose BOM** for modern UI framework
- **Kotlinx Serialization** for JSON data persistence
- **Material3** for design system components
- **Room Database** for task management with SQLite
- **SharedPreferences** for user preferences and writing entries
- **AlarmManager** for notification scheduling

**Major Modules Developed:**
1. **Task Management System** - CRUD operations with priority-based sorting
2. **Journal/Writing Module** - Mood tracking with JSON serialization and file attachments
3. **Reading Tracker** - Book progress management with status transitions
4. **Notification Service** - Custom reminder scheduling with AlarmManager
5. **Theme Management** - Dynamic light/dark theme switching
6. **Data Persistence Layer** - Hybrid storage using Room DB and SharedPreferences

**Design Deviations:** The original design utilized SharedPreferences for all data storage, but during implementation, tasks were migrated to Room Database for better performance and complex querying capabilities. Writing entries and books remained in SharedPreferences with JSON serialization to maintain simplicity and privacy requirements.

---

## 2. Implementation Evidence

### 2.1 Task Management Module

**Goal:** Implement a robust task management system with priority-based sorting, category filtering, and completion tracking using Room Database.

**Code Implementation:**
```kotlin
// TaskDao.kt - Database access object for task operations
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY 
        CASE priority 
            WHEN 'URGENT' THEN 1 
            WHEN 'HIGH' THEN 2 
            WHEN 'MEDIUM' THEN 3 
            WHEN 'LOW' THEN 4 
        END, createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    fun getIncompleteTasks(): Flow<List<Task>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Int, isCompleted: Boolean, completedAt: String)
}

// Task sorting algorithm implementation
fun sortTasksByPriority(tasks: List<Task>): List<Task> {
    return tasks.sortedWith(
        compareBy<Task> { task ->
            when (task.priority) {
                Priority.URGENT -> 1
                Priority.HIGH -> 2
                Priority.MEDIUM -> 3
                Priority.LOW -> 4
            }
        }.thenByDescending { it.createdAt }
    )
}
```

**Explanation:** The task management system uses Room Database for persistent storage with automatic priority-based sorting. The `TaskDao` interface defines database operations using SQL queries with custom sorting logic. The sorting algorithm demonstrates **algorithmic thinking** by implementing a multi-level comparison function that prioritizes urgent tasks first, then sorts by creation date.

**Screenshot Evidence:** *[Include screenshot of task list showing priority color coding and sorting]*

---

### 2.2 Journal/Writing Module with Mood Tracking

**Goal:** Create a comprehensive journaling system with mood tracking, file attachments, and JSON serialization for data persistence.

**Code Implementation:**
```kotlin
// WritingEntry data model with serialization
@Serializable
data class WritingEntry(
    val id: Int,
    val title: String,
    val content: String,
    val type: WritingType,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val checklists: List<ChecklistItem> = emptyList(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime
)

// Mood color mapping algorithm
object MoodUtils {
    private val moodColors = mapOf(
        "Happy" to Color(0xFF4CAF50),      // Green
        "Sad" to Color(0xFF2196F3),        // Blue
        "Angry" to Color(0xFFF44336),      // Red
        "Anxious" to Color(0xFFFF9800),    // Orange
        "Excited" to Color(0xFF9C27B0),    // Purple
        "Calm" to Color(0xFF00BCD4)        // Cyan
    )
    
    fun getMoodByName(moodName: String): Mood? {
        return moods.find { it.name.equals(moodName, ignoreCase = true) }
    }
    
    fun getMoodColor(moodName: String): Color {
        return moodColors[moodName] ?: Color.Gray
    }
}

// Data persistence with JSON serialization
private fun saveWritingEntries() {
    try {
        val jsonString = json.encodeToString(_writingEntries)
        prefs?.edit()?.putString("writing_entries", jsonString)?.apply()
        Log.d("DataManager", "Saved ${_writingEntries.size} writing entries")
    } catch (e: Exception) {
        Log.e("DataManager", "Error saving writing entries", e)
    }
}
```

**Explanation:** The journal module demonstrates **data abstraction** through well-structured data classes and **decomposition** by separating mood utilities, serialization logic, and UI components. The mood color mapping algorithm uses a hash map for efficient color lookup, showcasing **algorithmic thinking** in data structure selection.

**Screenshot Evidence:** *[Include screenshot showing mood-colored journal entries with file attachments]*

---

### 2.3 File Attachment System

**Goal:** Implement multi-format file attachment support with in-app viewing capabilities for images, PDFs, and documents.

**Code Implementation:**
```kotlin
// File type validation algorithm
fun validateFileType(fileUri: Uri, allowedTypes: List<String>): Boolean {
    val fileExtension = getFileExtension(fileUri)
    return allowedTypes.contains(fileExtension?.lowercase())
}

// Multi-format file viewer with type routing
@Composable
fun FileViewer(attachment: Attachment) {
    when (attachment.type) {
        AttachmentType.IMAGE -> ImageViewer(attachment)
        AttachmentType.FILE -> when (getFileExtension(attachment.name)) {
            "txt" -> TextFileViewer(attachment)
            "pdf" -> PDFViewer(attachment)
            "doc", "docx" -> DocumentViewer(attachment)
            else -> UnsupportedFileViewer(attachment)
        }
    }
}

// File extension detection algorithm
fun getFileExtension(fileName: String): String? {
    return fileName.substringAfterLast('.', "").takeIf { it.isNotEmpty() }
}
```

**Explanation:** The file attachment system demonstrates **decomposition** by breaking file handling into specialized viewers for different formats. The validation algorithm uses **logical reasoning** to check file types against allowed extensions, ensuring security and user experience.

**Screenshot Evidence:** *[Include screenshot showing file attachment viewing in action]*

---

### 2.4 Notification System with AlarmManager

**Goal:** Implement a reliable notification system for task reminders with custom timing and persistent scheduling.

**Code Implementation:**
```kotlin
// Notification scheduling algorithm
fun scheduleTaskNotificationAtTime(taskId: Int, reminderTime: LocalDateTime) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        action = "Task Reminder"
        putExtra("taskId", taskId)
        putExtra("title", "Task Reminder")
        putExtra("message", "Don't forget your task!")
    }
    
    val pendingIntent = PendingIntent.getBroadcast(
        context, 
        taskId, 
        intent, 
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    val triggerTime = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    
    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        triggerTime,
        pendingIntent
    )
    
    Log.d("NotificationScheduler", "Scheduled notification for task $taskId at $reminderTime")
}

// Broadcast receiver for notification delivery
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "Task Reminder" -> {
                val taskId = intent.getIntExtra("taskId", -1)
                val title = intent.getStringExtra("title") ?: "Task Reminder"
                val message = intent.getStringExtra("message") ?: "Don't forget your task!"
                
                notificationManager.showTaskReminderNotification(
                    TaskNotificationData(taskId, title, message)
                )
            }
        }
    }
}
```

**Explanation:** The notification system demonstrates **event-driven programming** through BroadcastReceiver implementation and **algorithmic thinking** in time calculation and scheduling logic. The system ensures reliable notification delivery even when the app is closed.

**Screenshot Evidence:** *[Include screenshot of notification popup and scheduling interface]*

---

### 2.5 Theme Management System

**Goal:** Implement dynamic theme switching with manual toggle and persistent user preferences.

**Code Implementation:**
```kotlin
// Theme management with state persistence
class ThemeManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    fun toggleTheme() {
        val currentTheme = getCurrentTheme()
        val newTheme = when (currentTheme) {
            ThemeType.SYSTEM -> ThemeType.LIGHT
            ThemeType.LIGHT -> ThemeType.DARK
            ThemeType.DARK -> ThemeType.SYSTEM
        }
        setTheme(newTheme)
    }
    
    fun getCurrentTheme(): ThemeType {
        val themeString = prefs.getString("theme", ThemeType.SYSTEM.name) ?: ThemeType.SYSTEM.name
        return try {
            ThemeType.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            ThemeType.SYSTEM
        }
    }
    
    fun setTheme(theme: ThemeType) {
        prefs.edit().putString("theme", theme.name).apply()
    }
}

// Reactive theme application in Compose
@Composable
fun PathXTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Explanation:** The theme system demonstrates **abstraction** through the ThemeManager class and **event-driven programming** through reactive UI updates. The toggle algorithm uses state machine logic for cycling through theme options.

**Screenshot Evidence:** *[Include screenshots showing light and dark theme transitions]*

---

### 2.6 Data Manager Singleton Pattern

**Goal:** Implement centralized data management with reactive UI updates using singleton pattern and state observation.

**Code Implementation:**
```kotlin
// Singleton data manager with reactive state
object DataManager {
    private var context: Context? = null
    private var prefs: SharedPreferences? = null
    private val json = Json { ignoreUnknownKeys = true }
    
    // Reactive state lists for UI observation
    private val _writingEntries = mutableStateListOf<WritingEntry>()
    private val _books = mutableStateListOf<Book>()
    private val _projects = mutableStateListOf<Project>()
    
    fun initialize(context: Context) {
        this.context = context
        this.prefs = context.getSharedPreferences("pathx_data", Context.MODE_PRIVATE)
        loadPersistedData()
    }
    
    // Reactive data operations with automatic persistence
    fun addWritingEntry(entry: WritingEntry) {
        val newId = (_writingEntries.maxOfOrNull { it.id } ?: 0) + 1
        val newEntry = entry.copy(id = newId)
        _writingEntries.add(newEntry)
        saveWritingEntries() // Automatic persistence
    }
    
    // Observer pattern for UI reactivity
    fun getWritingEntriesObservable(): SnapshotStateList<WritingEntry> = _writingEntries
}
```

**Explanation:** The DataManager demonstrates **decomposition** by centralizing data operations and **abstraction** through the singleton pattern. The reactive state lists enable automatic UI updates when data changes, showcasing **event-driven programming**.

**Screenshot Evidence:** *[Include screenshot showing data persistence across app restarts]*

---

## 3. Key Programming Techniques

### 3.1 Algorithm Design - Priority-Based Task Sorting

**Computational Thinking Link:** **Algorithmic Thinking**

The task sorting algorithm implements a multi-level comparison function that demonstrates efficient sorting strategies:

```kotlin
// Custom comparator with priority weighting
fun sortTasksByPriority(tasks: List<Task>): List<Task> {
    return tasks.sortedWith(
        compareBy<Task> { task ->
            // Priority weight assignment
            when (task.priority) {
                Priority.URGENT -> 1    // Highest priority
                Priority.HIGH -> 2
                Priority.MEDIUM -> 3
                Priority.LOW -> 4       // Lowest priority
            }
        }.thenByDescending { it.createdAt } // Secondary sort by date
    )
}
```

**Complexity Analysis:** O(n log n) time complexity using Kotlin's optimized sorting algorithm. The algorithm demonstrates **algorithmic thinking** by implementing a stable sort that maintains task order within priority levels.

### 3.2 Data Abstraction - Serializable Data Models

**Computational Thinking Link:** **Abstraction**

The data models abstract complex user data into manageable, type-safe structures:

```kotlin
// Abstract data representation with serialization
@Serializable
data class WritingEntry(
    val id: Int,
    val title: String,
    val content: String,
    val type: WritingType,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime
)
```

**Abstraction Benefits:** Encapsulates complex journal entry data with validation, serialization, and type safety. The custom LocalDateTimeSerializer demonstrates **abstraction** by hiding serialization complexity from the data model.

### 3.3 Decomposition - MVVM Architecture

**Computational Thinking Link:** **Decomposition**

The application architecture separates concerns into distinct layers:

- **UI Layer:** Jetpack Compose screens and components
- **Business Logic:** DataManager and ViewModels
- **Data Layer:** Repository pattern with Room DB and SharedPreferences
- **Service Layer:** NotificationService and ThemeManager

```kotlin
// Example of decomposition in action
@Composable
fun WritingScreen() {
    // UI Layer - handles presentation
    val writingEntries = DataManager.getWritingEntries() // Business Logic
    
    // Decomposed UI components
    WritingEntryList(entries = writingEntries)
    AddEntryButton()
    FilterControls()
}
```

**Decomposition Benefits:** Each layer has a single responsibility, making the code maintainable, testable, and scalable.

### 3.4 Event-Driven Programming - Notification System

**Computational Thinking Link:** **Event-Driven Programming**

The notification system responds to events through Android's BroadcastReceiver pattern:

```kotlin
// Event-driven notification delivery
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Event handler for notification triggers
        when (intent.action) {
            "Task Reminder" -> handleTaskReminder(intent)
            "Boot Completed" -> rescheduleAllNotifications()
        }
    }
}
```

**Event-Driven Benefits:** Decouples notification scheduling from delivery, ensuring reliable operation even when the app is not running.

### 3.5 Data Management - Hybrid Storage Strategy

**Computational Thinking Link:** **Data Management**

The application uses different storage mechanisms optimized for different data types:

```kotlin
// Hybrid storage implementation
object DataManager {
    // Room Database for complex queries (tasks)
    private val taskDao: TaskDao = database.taskDao()
    
    // SharedPreferences for simple persistence (preferences)
    private val prefs: SharedPreferences = context.getSharedPreferences("pathx_data", Context.MODE_PRIVATE)
    
    // JSON serialization for complex objects (writing entries)
    private fun saveWritingEntries() {
        val jsonString = json.encodeToString(_writingEntries)
        prefs.edit().putString("writing_entries", jsonString).apply()
    }
}
```

**Data Management Strategy:** Demonstrates **logical reasoning** in selecting appropriate storage mechanisms based on data complexity and access patterns.

### 3.6 Validation Algorithm - File Type Filtering

**Computational Thinking Link:** **Logical Reasoning**

The file validation system ensures security and user experience through type checking:

```kotlin
// File type validation with security considerations
fun validateFileType(fileUri: Uri, allowedTypes: List<String>): Boolean {
    val fileExtension = getFileExtension(fileUri)
    val mimeType = context.contentResolver.getType(fileUri)
    
    return allowedTypes.contains(fileExtension?.lowercase()) && 
           isAllowedMimeType(mimeType)
}

// MIME type security check
private fun isAllowedMimeType(mimeType: String?): Boolean {
    val allowedMimeTypes = listOf(
        "image/*", "text/*", "application/pdf", 
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )
    
    return mimeType?.let { type ->
        allowedMimeTypes.any { allowed -> 
            if (allowed.endsWith("/*")) {
                type.startsWith(allowed.dropLast(1))
            } else {
                type == allowed
            }
        }
    } ?: false
}
```

**Logical Reasoning:** Implements defense-in-depth security by checking both file extensions and MIME types to prevent malicious file uploads.

---

## 4. Challenges and Solutions

| Challenge | Solution | Computational Concept |
|-----------|----------|----------------------|
| **Data Loss on App Restart** | Implemented comprehensive SharedPreferences persistence with JSON serialization and first-launch detection | **Data Management** - Ensured data integrity through proper serialization |
| **LocalDateTime Serialization Errors** | Created custom LocalDateTimeSerializer with ISO format handling and error recovery | **Error Handling** - Robust serialization with fallback mechanisms |
| **Notification Delivery Reliability** | Added BOOT_COMPLETED receiver to reschedule alarms after device restart | **Event-Driven Programming** - System event handling for persistent functionality |
| **Theme Toggle UI Reactivity** | Implemented reactive state management with callback propagation through component hierarchy | **Abstraction** - State management abstraction for UI consistency |
| **File Attachment Memory Issues** | Used URI references instead of copying files, implemented efficient image loading | **Resource Management** - Optimized memory usage through lazy loading |
| **Database Migration Crashes** | Added fallbackToDestructiveMigration() and version management | **Error Handling** - Graceful handling of schema changes |

### 4.1 Critical Bug Fix - Data Persistence

**Problem:** User data was disappearing when the app was removed from recent apps and reopened.

**Root Cause Analysis:** The `init` block in DataManager was calling `initializeDefaultData()` before checking for persisted data, causing example data to overwrite user data on every app start.

**Solution Implementation:**
```kotlin
// Fixed initialization logic
private fun loadPersistedData() {
    val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
    
    if (isFirstLaunch) {
        // Only load example data on first launch
        initializeDefaultData()
        prefs.edit().putBoolean("is_first_launch", false).apply()
    } else {
        // Load saved user data
        loadSavedData()
    }
}
```

**Computational Concept:** **Logical Reasoning** - Implemented proper state management to distinguish between first launch and subsequent launches.

---

## 5. Testing During Development

### 5.1 Unit Testing Evidence

**Test Coverage Areas:**
- Data serialization/deserialization
- Task sorting algorithms
- File type validation
- Notification scheduling logic

**Example Test Case:**
```kotlin
@Test
fun testTaskSortingByPriority() {
    val tasks = listOf(
        Task(1, "Low Priority Task", Priority.LOW, false),
        Task(2, "Urgent Task", Priority.URGENT, false),
        Task(3, "High Priority Task", Priority.HIGH, false)
    )
    
    val sortedTasks = sortTasksByPriority(tasks)
    
    assertEquals("Urgent Task", sortedTasks[0].title)
    assertEquals("High Priority Task", sortedTasks[1].title)
    assertEquals("Low Priority Task", sortedTasks[2].title)
}
```

### 5.2 Manual Testing Protocol

**Testing Matrix:**

| Feature | Test Case | Expected Result | Actual Result | Status |
|---------|-----------|-----------------|---------------|--------|
| Task Creation | Add task with high priority | Task appears at top of list with red color | ✅ Pass | ✅ |
| Mood Tracking | Select "Happy" mood | Journal entry shows green mood chip | ✅ Pass | ✅ |
| File Attachment | Attach PDF document | File icon appears, opens in external app | ✅ Pass | ✅ |
| Theme Toggle | Switch between light/dark | UI updates immediately with new colors | ✅ Pass | ✅ |
| Notification | Set reminder for 5 minutes | Notification appears at scheduled time | ✅ Pass | ✅ |
| Data Persistence | Close app, remove from recent apps, reopen | All user data remains intact | ✅ Pass | ✅ |

**Device Testing:** Tested on Android emulators (API levels 29-34) and physical devices to ensure compatibility across different Android versions.

### 5.3 Integration Testing

**End-to-End Test Scenarios:**
1. **Complete User Journey:** Create task → Set reminder → Receive notification → Mark complete
2. **Data Flow Testing:** Add journal entry → Verify mood color → Check file attachment → Confirm persistence
3. **Theme Integration:** Switch themes → Verify all components update → Check persistence across app restarts

**Screenshot Evidence:** *[Include screenshots of testing results and app functionality]*

---

## 6. Reflection on Design Changes

### 6.1 Major Design Modifications

**Change 1: Storage Strategy Evolution**
- **Original Design:** SharedPreferences for all data types
- **Implementation:** Hybrid approach with Room Database for tasks, SharedPreferences for writing entries
- **Justification:** Tasks required complex queries and sorting that Room Database handles more efficiently than JSON parsing. This demonstrates **computational thinking** in selecting appropriate tools for specific problems.

**Change 2: Notification System Enhancement**
- **Original Design:** Simple notification scheduling
- **Implementation:** Custom time selection with popup dialog and persistent scheduling
- **Justification:** Client feedback indicated need for more control over reminder timing. The popup interface improves UX compared to inline settings.

**Change 3: Special User Features Addition**
- **Original Design:** Generic user experience
- **Implementation:** Personalized features for specific users (Calvin's racing car theme)
- **Justification:** Client requested personalized experience. This demonstrates **abstraction** in creating a flexible user type system.

### 6.2 Computational Reasoning in Design Decisions

**Data Structure Selection:** Choosing `mutableStateListOf` for reactive UI updates demonstrates understanding of **event-driven programming** principles.

**Algorithm Optimization:** Implementing priority-based sorting with secondary date sorting shows **algorithmic thinking** in creating efficient solutions.

**Architecture Patterns:** Using singleton pattern for DataManager and repository pattern for data access demonstrates **decomposition** and **abstraction** in software design.

### 6.3 Client Consultation Impact

Each design change was driven by client feedback and iterative development:
- Mood tracking enhancement (Client: "I want to track emotions")
- File attachment support (Client: "Can I attach photos?")
- Dark theme refinement (Client: "Colors don't feel right")
- Notification customization (Client: "Need more control over reminders")

These changes demonstrate the iterative nature of software development and the importance of **user-centered design** in computational thinking.

---

## Success Criteria Alignment

The implementation directly addresses all success criteria established in Criterion A:

1. **Task Management with Priority System** ✅
   - Room Database implementation with priority-based sorting
   - Color-coded priority indicators in UI
   - Completion tracking with timestamps

2. **Mood-Based Journal Writing** ✅
   - Comprehensive mood selection with color mapping
   - JSON serialization for data persistence
   - Mood-colored UI components

3. **Reading Progress Tracking** ✅
   - Book status management (To Read, Reading, Completed)
   - Progress tracking with page counts
   - Rating and notes system

4. **File Attachment Support** ✅
   - Multi-format file support (images, PDFs, documents)
   - In-app viewing capabilities
   - Secure file type validation

5. **Notification System** ✅
   - Custom reminder time selection
   - Reliable delivery using AlarmManager
   - Persistent scheduling across app restarts

6. **Theme Management** ✅
   - Manual theme toggle functionality
   - Custom color palettes for both themes
   - Persistent theme preferences

7. **Data Persistence** ✅
   - Hybrid storage strategy ensuring data survival
   - Comprehensive serialization for complex objects
   - First-launch detection preventing data loss

The implementation demonstrates **computational thinking** throughout, with clear evidence of **abstraction**, **decomposition**, **algorithmic thinking**, and **logical reasoning** in the development process.

