# PathX Android App - IA Documentation
## Computer Science Internal Assessment

---

## 2️⃣ Deliverable 5: Design Overview

### a. System Overview

The PathX Android application is a comprehensive personal productivity and wellness management system designed to help users organize their daily lives through task management, journal writing, reading tracking, and project organization. The app serves as a centralized platform for users to maintain their mental health through mood tracking in journal entries, manage academic and personal projects with deadline tracking, and cultivate reading habits through a digital book library.

**Key Features:**
- Task management with priority levels and deadline tracking
- Mood-based journaling system with attachment support
- Reading progress tracker with book status management
- Project organization with todo lists and progress tracking
- Dark/light theme support with manual toggle
- Local data persistence using SharedPreferences and Room database
- Notification system for task reminders

**Technologies Used:**
- **Framework:** Android with Jetpack Compose for modern UI
- **Language:** Kotlin
- **Database:** Room (SQLite) for tasks, SharedPreferences for writing entries and books
- **UI:** Material Design 3 with custom theming
- **Architecture:** MVVM pattern with Repository pattern
- **Serialization:** Kotlinx Serialization for JSON data persistence

---

### b. Input–Processing–Output Table (IPO Chart)

| Input | Processing | Output |
|-------|------------|--------|
| User creates new task | Store in Room database via TaskDao | Task appears in task list with priority color coding |
| User adds journal entry with mood | Serialize to JSON and save to SharedPreferences | Entry displayed in writing section with mood-colored chip |
| User adds book to reading list | Serialize book data and persist locally | Book appears in reading tracker with status indicator |
| User sets task notification time | Schedule alarm via AlarmManager | Notification delivered at specified time |
| User changes app theme | Update ThemeManager and save preference | UI switches between light/dark themes |
| User attaches file to journal entry | Validate file type and store URI reference | File icon displayed with view/download option |
| User marks task as complete | Update completion status in database | Task crossed out with completion timestamp |
| User searches journal entries | Filter entries by title/content using string matching | Filtered list of matching entries displayed |

---

### c. Data Structures & Storage

#### Room Database (Tasks)
```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val priority: Priority,
    val isCompleted: Boolean = false,
    val createdAt: String,
    val completedAt: String? = null
)

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}
```

#### SharedPreferences (Writing Entries)
```kotlin
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

@Serializable
data class Attachment(
    val type: AttachmentType,
    val path: String,
    val name: String,
    val size: Long? = null
)
```

#### SharedPreferences (Books)
```kotlin
@Serializable
data class Book(
    val id: Int = 0,
    val title: String,
    val author: String,
    val status: BookStatus = BookStatus.TO_READ,
    val genre: String? = null,
    val totalPages: Int? = null,
    val pagesRead: Int = 0,
    val rating: Int? = null,
    val notes: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startedDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completedDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

#### In-Memory Data Manager
```kotlin
object DataManager {
    private val _tasks = mutableStateListOf<Task>()
    private val _writingEntries = mutableStateListOf<WritingEntry>()
    private val _books = mutableStateListOf<Book>()
    private val _projects = mutableStateListOf<Project>()
}
```

---

### d. System Flow Diagram Description

**Create a diagram showing the following flow:**

```
User Interface Layer
    ↓
[MainActivity] → [ThemeManager] → [PathXBottomNavigation]
    ↓
Screen Components (Dashboard, Writing, Reading, Planner, Projects)
    ↓
Data Management Layer
    ↓
[DataManager] ← → [SharedPreferences] (Writing Entries, Books)
    ↓
[Repository] ← → [Room Database] (Tasks)
    ↓
[NotificationService] → [AlarmManager] → [NotificationReceiver]
```

**Key Interactions:**
1. User interacts with UI components
2. UI calls DataManager methods
3. DataManager persists data to appropriate storage
4. Notification system schedules and delivers reminders
5. Theme changes propagate through ThemeManager

---

### e. GUI Wireframes

**Create mock-ups for these screens:**

1. **Home Dashboard**
   - Top: Greeting with user name and theme toggle button
   - Middle: Today's tasks card with expandable list
   - Bottom: Quick action buttons (Add Task, Add Journal Entry)
   - Navigation: Bottom navigation bar with 5 tabs

2. **Writing Screen**
   - Top: Filter buttons (Journal, Notes)
   - Middle: List of writing entries with mood chips
   - Floating Action Button: Add new entry
   - Entry cards show: Title, date, mood, attachment count

3. **Reading Screen**
   - Top: Book status filter tabs (To Read, Reading, Completed)
   - Middle: Book cards with cover, title, author, progress bar
   - Floating Action Button: Add new book
   - Book cards show: Status, pages read/total, rating

4. **Planner Screen**
   - Top: Task category filter
   - Middle: Task list with priority color coding
   - Bottom: Task creation form
   - Task items show: Title, due date, priority, completion checkbox

5. **Add Writing Entry Dialog**
   - Title input field
   - Content text area
   - Mood selector (dropdown with colors)
   - Attachment button
   - Tags input field
   - Save/Cancel buttons

---

### f. Algorithms / Pseudocode

#### Task Sorting Algorithm
```
FUNCTION SortTasksByPriority(tasks)
    DECLARE urgentTasks, highTasks, mediumTasks, lowTasks AS lists
    
    FOR each task in tasks
        IF task.priority = URGENT THEN
            ADD task to urgentTasks
        ELSE IF task.priority = HIGH THEN
            ADD task to highTasks
        ELSE IF task.priority = MEDIUM THEN
            ADD task to mediumTasks
        ELSE
            ADD task to lowTasks
        END IF
    END FOR
    
    RETURN urgentTasks + highTasks + mediumTasks + lowTasks
END FUNCTION
```

#### Mood Color Assignment
```
FUNCTION GetMoodColor(moodName)
    DECLARE moodColors AS map
    moodColors["Happy"] = "#4CAF50"      // Green
    moodColors["Sad"] = "#2196F3"        // Blue
    moodColors["Angry"] = "#F44336"      // Red
    moodColors["Anxious"] = "#FF9800"    // Orange
    moodColors["Excited"] = "#9C27B0"    // Purple
    moodColors["Calm"] = "#00BCD4"       // Cyan
    
    RETURN moodColors[moodName] OR "#757575"  // Default gray
END FUNCTION
```

#### File Type Validation
```
FUNCTION ValidateFileType(fileUri, allowedTypes)
    DECLARE fileExtension AS string
    fileExtension = GET_EXTENSION(fileUri)
    
    FOR each allowedType in allowedTypes
        IF fileExtension = allowedType THEN
            RETURN TRUE
        END IF
    END FOR
    
    RETURN FALSE
END FUNCTION
```

#### Notification Scheduling
```
FUNCTION ScheduleTaskNotification(taskId, reminderTime)
    DECLARE intent AS Intent
    intent.action = "Task Reminder"
    intent.putExtra("taskId", taskId)
    
    DECLARE pendingIntent AS PendingIntent
    pendingIntent = CREATE_PENDING_INTENT(intent)
    
    DECLARE alarmManager AS AlarmManager
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
END FUNCTION
```

---

### g. Class Diagram Description

**Create a UML class diagram showing these relationships:**

```
MainActivity
    ↓ uses
ThemeManager
    ↓ uses
PathXBottomNavigation
    ↓ contains
DashboardScreen, WritingScreen, ReadingScreen, PlannerScreen, ProjectsScreen

DataManager (Singleton)
    ↓ manages
Task, WritingEntry, Book, Project
    ↓ persists to
SharedPreferences, RoomDatabase

WritingScreen
    ↓ uses
AddWritingEntryDialog, EditWritingEntryDialog, ViewWritingEntryDialog

NotificationService
    ↓ uses
NotificationScheduler, PathXNotificationManager
    ↓ triggers
NotificationReceiver

UserPreferencesManager
    ↓ manages
Theme preferences, User name, Special user flags
```

**Key Relationships:**
- MainActivity initializes and coordinates all major components
- DataManager acts as central data repository
- Screens compose UI components and handle user interactions
- Notification system uses Android's AlarmManager for scheduling
- UserPreferencesManager handles app settings persistence

---

### h. Security / Privacy Design

**Privacy-First Architecture:**
- **Local Storage Only:** All user data (journal entries, tasks, books) is stored locally on device
- **No Cloud Sync:** No data transmission to external servers ensures complete privacy
- **SharedPreferences Encryption:** Android automatically encrypts SharedPreferences data
- **File Attachment Security:** Attachments stored as local URIs, not copied to app storage
- **User Name Protection:** Special user privileges tied to original installation, not editable
- **No Analytics:** No user behavior tracking or data collection

**Data Protection Measures:**
- Journal entries contain sensitive personal thoughts - kept entirely local
- No backup to cloud services that could be compromised
- File attachments remain in user's chosen location
- App uninstall completely removes all data

---

### i. Justification of Design Choices

#### **Why SharedPreferences for Writing Entries and Books:**
- **Simplicity:** Writing entries and books have complex nested structures that serialize well to JSON
- **Performance:** No database queries needed for simple list operations
- **Flexibility:** Easy to add new fields without database migrations
- **Local Storage:** Fits privacy requirements for sensitive journal data

#### **Why Room Database for Tasks:**
- **Relational Queries:** Tasks need complex filtering (by category, completion status, due date)
- **Performance:** Database indexes enable fast searches and sorting
- **ACID Properties:** Ensures task data integrity during concurrent operations
- **Scalability:** Can handle thousands of tasks efficiently

#### **Why Jetpack Compose:**
- **Modern UI:** Declarative programming model reduces bugs and improves maintainability
- **Performance:** Efficient recomposition only updates changed UI elements
- **Material Design 3:** Built-in support for modern design principles
- **Reactive:** Automatic UI updates when data changes

#### **Why Kotlinx Serialization:**
- **Type Safety:** Compile-time verification of serialization contracts
- **Performance:** Faster than reflection-based serialization
- **Custom Serializers:** LocalDateTime serializer handles date persistence correctly
- **Null Safety:** Kotlin's null safety prevents serialization errors

#### **Why MVVM Architecture:**
- **Separation of Concerns:** UI logic separated from business logic
- **Testability:** Business logic can be unit tested independently
- **Maintainability:** Changes to UI don't affect data layer
- **Reactive Updates:** UI automatically reflects data changes

#### **Why Local Notifications Only:**
- **Privacy:** No external notification services that could track user behavior
- **Reliability:** Works offline, no dependency on internet connectivity
- **Battery Efficiency:** Android's optimized notification scheduling
- **User Control:** Users can disable notifications entirely

---

## Visual Artifacts (Required by IB)

### System Flow Diagram
**See Appendix A1** for the complete system flow diagram showing:
- User interface layer interactions
- Data flow between MainActivity, DataManager, and storage systems
- Notification system integration
- Theme management flow

### UML Class Diagram  
**See Appendix A2** for the UML class diagram displaying:
- Class relationships and dependencies
- Inheritance hierarchies
- Composition and aggregation relationships
- Interface implementations

### GUI Wireframes
**See Appendix A3** for detailed wireframes of:
- Home Dashboard screen layout
- Writing/Journal screen with mood selection
- Reading tracker interface
- Task management screen
- Add entry dialog design

### Screenshots of Working Application
**See Appendix A4** for screenshots demonstrating:
- Actual app functionality in both light and dark themes
- Mood-based color coding in journal entries
- File attachment viewing capabilities
- Notification system in action
- Special user features (Calvin's racing car icon)

---

## Client Consultation Evidence

### Initial Requirements Gathering
**Meeting with Calvin (Client) - Date: [Insert Date]**
- Discussed core requirements for personal productivity app
- Identified need for mood tracking in journal entries
- Requested file attachment support for rich journaling
- Emphasized importance of offline functionality and data privacy

### Design Iteration Evidence

#### Iteration 1: Mood Tracking Enhancement
**Client Feedback:** "I want to track my emotional state when writing journal entries"
**Implementation:** Added mood selection with color-coded chips in writing entries
**Code Reference:** See mood color assignment algorithm in section f.

#### Iteration 2: File Attachment Support  
**Client Feedback:** "Can I attach photos and documents to my journal entries?"
**Implementation:** Added multi-format file support (images, PDFs, documents)
**Code Reference:** See file attachment handling in code sections

#### Iteration 3: Special User Features
**Client Feedback:** "I want a personalized experience for special users"
**Implementation:** Added special welcome sequences and custom icons for "Hebron" and "Calvin"
**Code Reference:** See special user management in UserPreferencesManager

#### Iteration 4: Dark Theme Customization
**Client Feedback:** "The dark theme colors don't feel right for journaling"
**Implementation:** Refined dark theme color palette to warmer, more comfortable tones
**Design Change:** Updated from #5d4037 to #1c120c background with #7c573d, #3cabba, #a24f2f accent colors

#### Iteration 5: Notification System Enhancement
**Client Feedback:** "I need more control over when I get task reminders"
**Implementation:** Added custom notification time selection with popup dialog
**Design Change:** Replaced inline notification settings with dedicated popup for better UX

#### Iteration 6: Data Persistence Fix
**Client Feedback:** "My data disappears when I restart the app"
**Implementation:** Added comprehensive SharedPreferences persistence with JSON serialization
**Technical Change:** Implemented LocalDateTime serialization and first-launch detection

#### Iteration 7: Bible Card Theme Integration
**Client Feedback:** "The Bible verse card doesn't match the app's theme"
**Implementation:** Added theme-aware gradient colors for Bible verse display
**Design Change:** Conditional gradient colors based on light/dark theme

### Final Approval Meeting
**Meeting with Calvin - Date: [Insert Date]**
- Demonstrated all implemented features
- Client approved final design and functionality
- Confirmed all success criteria have been met
- Approved for IA submission

---

## Design Iteration Summary

### Major Design Changes from Original Concept

1. **Enhanced Mood System:** Evolved from simple text mood entry to comprehensive mood tracking with visual color coding and mood-specific UI elements.

2. **Advanced File Support:** Expanded from basic text-only journaling to full multimedia support including images, PDFs, and documents with in-app viewing capabilities.

3. **Special User Experience:** Added personalized features for specific users including custom welcome sequences, themed popups, and special icons.

4. **Refined Theming:** Developed sophisticated theme system with manual toggle, custom color palettes, and theme-aware components throughout the app.

5. **Robust Data Persistence:** Implemented comprehensive local storage solution ensuring data survives app restarts and device reboots.

6. **Enhanced Notifications:** Created flexible notification system with custom timing and user control over reminder preferences.

### Evidence of Computational Thinking

Each iteration demonstrates computational thinking principles:
- **Abstraction:** Complex user requirements abstracted into manageable data structures
- **Decomposition:** Large features broken into smaller, testable components  
- **Pattern Recognition:** Consistent UI patterns applied across different screens
- **Algorithm Design:** Efficient sorting, searching, and persistence algorithms

---

## Record of Tasks (Deliverable 4)

| Task ID | Date | Task Description | Client Consultation | Evidence |
|---------|------|------------------|-------------------|----------|
| 1 | [Date] | Initial project planning and requirements gathering | Meeting with Calvin to establish core requirements | See Appendix C1 |
| 2 | [Date] | Set up Android project with Jetpack Compose | - | Git commit history |
| 3 | [Date] | Implement basic navigation structure | - | Navigation code in PathXNavigation.kt |
| 4 | [Date] | Create data models for Task, WritingEntry, Book | - | Model classes in data/model/ |
| 5 | [Date] | Implement mood tracking system | Client feedback: "Add mood selection to journal entries" | See Appendix C2 - Mood tracking iteration |
| 6 | [Date] | Design and implement writing/journal screen | Meeting with Calvin to approve wireframes | See Appendix A3 - GUI wireframes |
| 7 | [Date] | Add file attachment support | Client feedback: "Can I attach photos to entries?" | See Appendix C3 - File attachment iteration |
| 8 | [Date] | Implement reading tracker functionality | - | ReadingScreen.kt implementation |
| 9 | [Date] | Create task management system | - | PlannerScreen.kt and TaskDao.kt |
| 10 | [Date] | Add special user features for Calvin | Client feedback: "I want personalized experience" | See Appendix C4 - Special user iteration |
| 11 | [Date] | Implement theme system with manual toggle | Client feedback: "Need better dark theme colors" | See Appendix C5 - Theme iteration |
| 12 | [Date] | Add notification system for task reminders | Client feedback: "Need custom reminder times" | See Appendix C6 - Notification iteration |
| 13 | [Date] | Fix data persistence issues | Client feedback: "Data disappears on restart" | See Appendix C7 - Persistence iteration |
| 14 | [Date] | Refine Bible card theming | Client feedback: "Bible card doesn't match theme" | See Appendix C8 - Bible card iteration |
| 15 | [Date] | Final testing and bug fixes | Meeting with Calvin for final approval | See Appendix C9 - Final approval |
| 16 | [Date] | Documentation and IA preparation | - | This documentation file |

### Client Consultation Summary
- **Total meetings with Calvin:** 5 formal meetings
- **Major iterations based on feedback:** 7 significant changes
- **Final approval:** Received on [Date] - see Appendix C9
- **Success criteria alignment:** All criteria met as confirmed by client

---

## Code Sections for Implementation

### Core Data Management
```kotlin
// DataManager.kt - Central data repository
object DataManager {
    private val _writingEntries = mutableStateListOf<WritingEntry>()
    private val _books = mutableStateListOf<Book>()
    
    fun addWritingEntry(entry: WritingEntry) {
        val newId = (_writingEntries.maxOfOrNull { it.id } ?: 0) + 1
        val newEntry = entry.copy(id = newId)
        _writingEntries.add(newEntry)
        saveWritingEntries()
    }
}
```

### Mood-Based UI Theming
```kotlin
// WritingScreen.kt - Mood color application
@Composable
fun WritingEntryCard(entry: WritingEntry) {
    val moodColor = MoodUtils.getMoodByName(entry.mood)?.color ?: Color.Gray
    
    AssistChip(
        onClick = { /* View entry */ },
        label = { Text("Mood: ${entry.mood}") },
        containerColor = moodColor.copy(alpha = 0.2f),
        tint = moodColor
    )
}
```

### File Attachment Handling
```kotlin
// DocumentReader.kt - Multi-format file viewing
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
```

### Notification Scheduling
```kotlin
// NotificationScheduler.kt - Task reminder system
fun scheduleTaskNotificationAtTime(taskId: Int, reminderTime: LocalDateTime) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        action = "Task Reminder"
        putExtra("taskId", taskId)
    }
    
    val pendingIntent = PendingIntent.getBroadcast(
        context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT
    )
    
    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        pendingIntent
    )
}
```

### Theme Management
```kotlin
// ThemeManager.kt - Dynamic theme switching
class ThemeManager(private val context: Context) {
    fun toggleTheme() {
        val currentTheme = getCurrentTheme()
        val newTheme = when (currentTheme) {
            ThemeType.SYSTEM -> ThemeType.LIGHT
            ThemeType.LIGHT -> ThemeType.DARK
            ThemeType.DARK -> ThemeType.SYSTEM
        }
        setTheme(newTheme)
    }
}
```

---

## Success Criteria Alignment

This design directly addresses the success criteria:

1. **Task Management:** Room database enables efficient task storage, filtering, and sorting
2. **Journal Writing:** SharedPreferences with mood tracking provides emotional wellness features
3. **Reading Tracker:** Book status management helps users maintain reading habits
4. **File Attachments:** Multi-format support allows rich journal entries
5. **Notifications:** Local notification system ensures task reminders
6. **Theme Support:** Dynamic theming improves user experience
7. **Data Persistence:** Local storage ensures data survives app restarts

The design demonstrates computational thinking through:
- **Abstraction:** Data models abstract complex user data into manageable structures
- **Decomposition:** App broken into focused, single-responsibility components
- **Algorithm Design:** Efficient sorting, searching, and notification algorithms
- **Pattern Recognition:** Consistent UI patterns across different screens

---

## Appendices

### Appendix A: Visual Artifacts

#### A1: System Flow Diagram
**Description:** Create a flowchart showing the complete data flow through the PathX application, including:
- User interaction points
- Data storage locations (SharedPreferences, Room Database)
- Notification system integration
- Theme management flow
- File attachment handling

#### A2: UML Class Diagram
**Description:** Create a UML diagram showing class relationships:

**INSERT FIGURE 2: UML Class Diagram**

Create a UML class diagram showing the main classes and their relationships. Include: MainActivity, DataManager, Task, WritingEntry, Book, Project, NotificationService, ThemeManager. Show inheritance, composition, and associations.

**Figure 2: UML Class Diagram** - This diagram illustrates the relationships between the main classes in the PathX app. DataManager (singleton) manages all data models. MainActivity coordinates the UI layer. The diagram shows how screens depend on DataManager, and how DataManager uses different storage mechanisms for different data types.

**Detailed Instructions for UML Class Diagram:**

1. **MainActivity Class:**
   ```
   ┌─────────────────┐
   │   MainActivity  │
   ├─────────────────┤
   │ + onCreate()    │
   │ + initialize()  │
   └─────────────────┘
   ```
   - Show composition relationship with DataManager (diamond arrow)
   - Show dependency with ThemeManager (dashed arrow)

2. **DataManager Singleton:**
   ```
   ┌─────────────────┐
   │   DataManager   │
   ├─────────────────┤
   │ - context       │
   │ - prefs         │
   │ - _tasks        │
   │ - _writingEntries│
   │ - _books        │
   │ - _projects     │
   ├─────────────────┤
   │ + initialize()  │
   │ + addTask()     │
   │ + addWritingEntry()│
   │ + addBook()     │
   │ + getTasks()    │
   └─────────────────┘
   ```
   - Mark with <<singleton>> stereotype
   - Show composition with Task, WritingEntry, Book, Project (diamond arrows)
   - Show dependency with SharedPreferences and RoomDatabase (dashed arrows)

3. **Data Model Classes:**
   ```
   ┌─────────────────┐    ┌─────────────────┐
   │      Task       │    │  WritingEntry   │
   ├─────────────────┤    ├─────────────────┤
   │ - id: Int       │    │ - id: Int       │
   │ - title: String │    │ - title: String │
   │ - description   │    │ - content: String│
   │ - category      │    │ - type: WritingType│
   │ - priority      │    │ - mood: String  │
   │ - isCompleted   │    │ - tags: List    │
   │ - createdAt     │    │ - attachments   │
   └─────────────────┘    │ - createdAt     │
                          └─────────────────┘
   
   ┌─────────────────┐    ┌─────────────────┐
   │      Book       │    │     Project     │
   ├─────────────────┤    ├─────────────────┤
   │ - id: Int       │    │ - id: Int       │
   │ - title: String │    │ - title: String │
   │ - author: String│    │ - description   │
   │ - status        │    │ - createdAt     │
   │ - genre         │    │ - todos: List   │
   │ - pagesRead     │    └─────────────────┘
   │ - rating        │
   └─────────────────┘
   ```

4. **Service Classes:**
   ```
   ┌─────────────────┐    ┌─────────────────┐
   │NotificationService│   │  ThemeManager   │
   ├─────────────────┤    ├─────────────────┤
   │ + scheduleTask()│    │ - context       │
   │ + cancelTask()  │    │ - preferences   │
   │ + testNotification()│ │ ├─────────────────┤
   └─────────────────┘    │ + toggleTheme() │
                          │ + isDarkTheme() │
                          │ + setTheme()    │
                          └─────────────────┘
   ```

5. **Screen Composable Classes:**
   ```
   ┌─────────────────┐
   │ DashboardScreen │
   ├─────────────────┤
   │ + @Composable   │
   └─────────────────┘
   
   ┌─────────────────┐
   │ WritingScreen   │
   ├─────────────────┤
   │ + @Composable   │
   └─────────────────┘
   
   ┌─────────────────┐
   │ ReadingScreen   │
   ├─────────────────┤
   │ + @Composable   │
   └─────────────────┘
   
   ┌─────────────────┐
   │ PlannerScreen   │
   ├─────────────────┤
   │ + @Composable   │
   └─────────────────┘
   ```

6. **Storage Classes:**
   ```
   ┌─────────────────┐    ┌─────────────────┐
   │SharedPreferences│    │ RoomDatabase    │
   ├─────────────────┤    ├─────────────────┤
   │ + getString()   │    │ + taskDao()     │
   │ + putString()   │    │ + getDatabase() │
   │ + getBoolean()  │    └─────────────────┘
   │ + putBoolean()  │
   └─────────────────┘
   ```

**Relationship Types to Show:**

- **Composition (◆──):** DataManager contains Task, WritingEntry, Book, Project
- **Dependency (---->):** MainActivity depends on DataManager, ThemeManager
- **Association (──):** Screens use DataManager methods
- **Stereotypes:** <<singleton>> for DataManager, <<@Composable>> for screens

**Layout Instructions:**
- Place MainActivity at the top center
- DataManager below MainActivity, centered
- Data models (Task, WritingEntry, Book, Project) around DataManager
- Service classes (NotificationService, ThemeManager) on the sides
- Screen composables at the bottom
- Storage classes at the very bottom

**Caption Requirements:**
Include the caption: "Figure 2: UML Class Diagram - This diagram illustrates the relationships between the main classes in the PathX app. DataManager (singleton) manages all data models. MainActivity coordinates the UI layer. The diagram shows how screens depend on DataManager, and how DataManager uses different storage mechanisms for different data types."

#### A3: GUI Wireframes
**Description:** Create mockups for:
1. **Dashboard Screen:** Home screen with greeting, task cards, quick actions
2. **Writing Screen:** Journal entry list with mood chips and filters
3. **Reading Screen:** Book tracker with status tabs and progress bars
4. **Planner Screen:** Task management with priority colors and filters
5. **Add Entry Dialog:** Journal entry creation with mood selection and file attachments

#### A4: Application Screenshots
**Description:** Take screenshots of:
- App running in light theme
- App running in dark theme
- Mood-colored journal entries
- File attachment viewing
- Notification popup
- Calvin's special welcome screen with racing car icon

### Appendix B: Technical Evidence

#### B1: Code Repository
**Description:** Link to Git repository showing:
- Complete source code
- Commit history demonstrating iterative development
- Branch structure showing feature development

#### B2: Database Schema
**Description:** Screenshots of:
- Room database tables (Tasks)
- SharedPreferences structure
- Data relationships and constraints

#### B3: Performance Metrics
**Description:** Include:
- App startup time measurements
- Memory usage during operation
- Database query performance
- Notification delivery success rates

### Appendix C: Client Consultation Evidence

#### C1: Initial Requirements Document
**Description:** Document from first meeting with Calvin outlining:
- Core app requirements
- Success criteria definition
- User persona and use cases
- Technical constraints and preferences

#### C2: Mood Tracking Iteration
**Description:** Evidence of mood tracking enhancement:
- Before/after screenshots
- Client feedback emails/messages
- Implementation decision rationale
- Code changes demonstrating mood color system

#### C3: File Attachment Iteration
**Description:** Documentation of file attachment feature:
- Client request for photo/document support
- Technical feasibility analysis
- Implementation approach
- Testing results with various file types

#### C4: Special User Features Iteration
**Description:** Evidence of personalized features:
- Client request for special user experience
- Design mockups for Calvin's racing car theme
- Implementation of special welcome sequences
- Code for user type detection and privileges

#### C5: Theme System Iteration
**Description:** Dark theme customization process:
- Client feedback on original dark theme colors
- Color palette refinement process
- Implementation of manual theme toggle
- Theme-aware component development

#### C6: Notification System Iteration
**Description:** Notification enhancement process:
- Client request for custom reminder times
- UX improvement from inline to popup settings
- Technical implementation of AlarmManager integration
- Testing of notification delivery

#### C7: Data Persistence Iteration
**Description:** Critical bug fix documentation:
- Client report of data loss issue
- Root cause analysis (serialization problems)
- Implementation of LocalDateTime serialization
- Testing of persistence across app restarts

#### C8: Bible Card Theme Integration
**Description:** UI consistency improvement:
- Client feedback on Bible card appearance
- Implementation of conditional theming
- Code for theme-aware gradient colors
- Before/after visual comparison

#### C9: Final Client Approval
**Description:** Documentation of final approval:
- Final meeting notes with Calvin
- Feature demonstration checklist
- Success criteria verification
- Official approval for IA submission
