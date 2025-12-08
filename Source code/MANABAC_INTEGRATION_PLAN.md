# Manabac Integration with PathX App

## 🎯 **Integration Overview**
Automatically sync assignments from Manabac to PathX app to streamline student workflow.

## 🔗 **Integration Methods**

### **1. API Integration (Preferred)**
**If Manabac provides API access:**

```kotlin
// New data model for Manabac assignments
@Entity(tableName = "manabac_assignments")
data class ManabacAssignment(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val dueDate: LocalDateTime,
    val subject: String,
    val priority: Priority,
    val isSynced: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Repository extension
class ManabacRepository(
    private val apiService: ManabacApiService
) {
    suspend fun syncAssignments(): List<ManabacAssignment> {
        return try {
            val response = apiService.getAssignments()
            response.map { it.toManabacAssignment() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

**Features:**
- ✅ Real-time sync
- ✅ Automatic assignment import
- ✅ Two-way sync (if supported)
- ✅ Offline capability

### **2. Web Scraping Integration**
**If no API available:**

```kotlin
class ManabacScraper {
    suspend fun scrapeAssignments(credentials: LoginCredentials): List<Assignment> {
        // Use WebView or HTTP client to scrape assignments
        // Parse HTML/JSON responses
        // Convert to PathX Task format
    }
}
```

**Features:**
- ✅ Works without API
- ⚠️ Requires login credentials
- ⚠️ May break if Manabac changes UI
- ✅ Can be automated with scheduling

### **3. Manual Import/Export**
**CSV/JSON file integration:**

```kotlin
class ManabacImportService {
    suspend fun importFromCsv(fileUri: Uri): List<Task> {
        // Parse CSV file exported from Manabac
        // Convert to PathX Task format
        // Bulk insert into database
    }
    
    suspend fun exportToManabac(tasks: List<Task>): String {
        // Convert PathX tasks to Manabac format
        // Generate export file
    }
}
```

**Features:**
- ✅ Simple implementation
- ✅ No API dependencies
- ✅ User controls data
- ⚠️ Manual process

## 🏗️ **Implementation Plan**

### **Phase 1: Basic Integration**
1. **Add Manabac Settings Screen**
   ```kotlin
   @Composable
   fun ManabacSettingsScreen() {
       // API key/credentials input
       // Sync frequency settings
       // Test connection button
   }
   ```

2. **Create Sync Service**
   ```kotlin
   class ManabacSyncService(
       private val repository: PathXRepository,
       private val manabacRepository: ManabacRepository
   ) {
       suspend fun syncAssignments() {
           val manabacAssignments = manabacRepository.getAssignments()
           val tasks = manabacAssignments.map { it.toTask() }
           repository.insertTasks(tasks)
       }
   }
   ```

3. **Add Sync Button to Dashboard**
   ```kotlin
   // Add sync button to dashboard
   IconButton(onClick = { syncManabacAssignments() }) {
       Icon(Icons.Default.Sync, contentDescription = "Sync Manabac")
   }
   ```

### **Phase 2: Advanced Features**
1. **Automatic Sync Scheduling**
   ```kotlin
   // Use WorkManager for background sync
   class ManabacSyncWorker : CoroutineWorker(context, params) {
       override suspend fun doWork(): Result {
           manabacSyncService.syncAssignments()
           return Result.success()
       }
   }
   ```

2. **Smart Assignment Mapping**
   ```kotlin
   // Auto-categorize assignments
   fun categorizeAssignment(title: String, subject: String): TaskCategory {
       return when {
           title.contains("SAT") -> TaskCategory.SAT_PREP
           subject.contains("Math") -> TaskCategory.ASSIGNMENT
           title.contains("Essay") -> TaskCategory.UNIVERSITY_DEADLINE
           else -> TaskCategory.ASSIGNMENT
       }
   }
   ```

3. **Duplicate Detection**
   ```kotlin
   suspend fun syncWithDuplicateDetection(newAssignments: List<ManabacAssignment>) {
       val existingTasks = repository.getAllTasks()
       val duplicates = findDuplicates(newAssignments, existingTasks)
       val newTasks = newAssignments.filter { !duplicates.contains(it) }
       repository.insertTasks(newTasks.map { it.toTask() })
   }
   ```

## 📱 **UI Enhancements**

### **Dashboard Integration**
```kotlin
@Composable
fun DashboardScreen() {
    // Add Manabac sync status
    ManabacSyncStatusCard()
    
    // Show synced assignments
    LazyColumn {
        item {
            Text("Synced from Manabac", style = MaterialTheme.typography.headlineSmall)
        }
        items(manabacTasks) { task ->
            TaskCard(task = task, source = "Manabac")
        }
    }
}
```

### **Settings Integration**
```kotlin
@Composable
fun SettingsScreen() {
    Column {
        SettingsSection("Manabac Integration") {
            SwitchSetting(
                title = "Auto-sync Assignments",
                checked = autoSyncEnabled,
                onCheckedChange = { /* Toggle auto-sync */ }
            )
            TimePickerSetting(
                title = "Sync Time",
                time = syncTime,
                onTimeChange = { /* Update sync time */ }
            )
        }
    }
}
```

## 🔐 **Security Considerations**

1. **Credential Storage**
   ```kotlin
   // Use Android Keystore for secure storage
   class SecureCredentialStorage {
       suspend fun storeCredentials(credentials: LoginCredentials)
       suspend fun getCredentials(): LoginCredentials?
       suspend fun clearCredentials()
   }
   ```

2. **Data Privacy**
   - Encrypt sensitive data
   - Local storage only
   - User consent for data sync
   - GDPR compliance

## 🚀 **Future Enhancements**

1. **Grade Integration**
   - Sync completed assignments with grades
   - Progress tracking across platforms

2. **Calendar Integration**
   - Sync due dates with Google Calendar
   - Reminder notifications

3. **Multi-Platform Support**
   - Support for other school platforms
   - Universal assignment format

## 📋 **Next Steps**

1. **Research Manabac API**
   - Check if Manabac provides API access
   - Review documentation and authentication

2. **Prototype Development**
   - Create basic sync functionality
   - Test with sample data

3. **User Testing**
   - Gather feedback from students
   - Refine sync logic and UI

Would you like me to start implementing any of these integration features?

