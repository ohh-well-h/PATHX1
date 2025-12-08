package com.example.pathx01.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pathx01.R
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.model.Priority
import com.example.pathx01.data.DataManager
import com.example.pathx01.ui.components.BibleVerseCard
import com.example.pathx01.ui.components.TaskCard
import com.example.pathx01.ui.theme.ForestGreen
import com.example.pathx01.utils.UserPreferencesManager
import com.example.pathx01.utils.ThemeManager
import com.example.pathx01.utils.GreetingUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    themeManager: ThemeManager,
    onThemeChanged: () -> Unit,
    onNavigateToPlanner: (String?) -> Unit = {},
    onNavigateToProjects: () -> Unit = {}
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    
    // Get user name and create personalized greeting (reactive to changes)
    var userName by remember { mutableStateOf(userPreferencesManager.getUserName()) }
    val greeting = remember(userName) { GreetingUtils.getTimeBasedGreeting(userName) }
    val greetingIconRes = remember { GreetingUtils.getGreetingIconRes() }
    
    // Use shared data from DataManager - access directly to ensure recomposition
    val allTasks = DataManager.getTasks()
    val allProjects = DataManager.getProjects()
    val allWritingEntries = DataManager.getWritingEntries()
    
    // State for dropdowns
    var todaysTasksExpanded by remember { mutableStateOf(false) }
    var thisWeekTasksExpanded by remember { mutableStateOf(false) }
    
    // State for name editing
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf("") }
    
    // Debug: Force save on first composition (temporarily disabled)
    /*
    LaunchedEffect(Unit) {
        DataManager.debugPrintState()
    }
    */
    
    // Debug buttons for testing persistence
    var showDebugButtons by remember { mutableStateOf(false) }
    
    // State for special user popups
    var showHebronSpecialPopup by remember { mutableStateOf(false) }
    var showCalvinSpecialPopup by remember { mutableStateOf(false) }
    
    // State for getting started auto-updates
    var hasAddedTask by remember { mutableStateOf(allTasks.isNotEmpty()) }
    var hasCreatedProject by remember { mutableStateOf(allProjects.isNotEmpty()) }
    var hasLoggedBook by remember { mutableStateOf(false) }
    var hasWrittenJournal by remember { mutableStateOf(false) }
    
    // Calculate dates properly
    val today = LocalDate.now()
    val startOfWeek = today.minusDays((today.dayOfWeek.value - 1).toLong()) // Monday
    val endOfWeek = startOfWeek.plusDays(6) // Sunday
    
    val todaysTasks = allTasks.filter { task ->
        task.dueDate?.let { dueDate ->
            try {
                val taskDate = LocalDateTime.parse(dueDate).toLocalDate()
                taskDate == today
            } catch (e: Exception) {
                false
            }
        } ?: false
    }
    
    val thisWeekTasks = allTasks.filter { task ->
        task.dueDate?.let { dueDate ->
            try {
                val taskDate = LocalDateTime.parse(dueDate).toLocalDate()
                taskDate.isAfter(today) && taskDate.isBefore(endOfWeek.plusDays(1))
            } catch (e: Exception) {
                false
            }
        } ?: false
    }
    
    val completedTasks = allTasks.count { it.isCompleted }.toLong()
    val pendingTasks = allTasks.count { !it.isCompleted }.toLong()
    val totalTasks = allTasks.size.toLong()
    
    // Calculate completion based on todos (100% completion = all todos completed)
    val completedProjects = allProjects.count { project ->
        project.todos.isNotEmpty() && project.todos.all { it.isCompleted }
    }
    val activeProjects = allProjects.count { project ->
        project.todos.isNotEmpty() && !project.todos.all { it.isCompleted }
    }
    val totalProjects = allProjects.size
    
    
    var showInstructions by remember { mutableStateOf(false) }
    var showDataDropdown by remember { mutableStateOf(false) }
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }
    var showClearExampleConfirmDialog by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // 2xl spacing from DayFlow design system
            verticalArrangement = Arrangement.spacedBy(24.dp) // 2xl spacing
        ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme toggle button
                    IconButton(
                        onClick = { 
                            themeManager.toggleTheme()
                            onThemeChanged()
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                when (themeManager.getThemeIcon()) {
                                    "sun" -> R.drawable.ic_sun
                                    "moon" -> R.drawable.ic_moon
                                    else -> R.drawable.ic_moon
                                }
                            ),
                            contentDescription = "Toggle Theme"
                        )
                    }
                    
                    // Data management dropdown
                    Box {
                        IconButton(onClick = { showDataDropdown = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_list),
                                contentDescription = "Data Management"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showDataDropdown,
                            onDismissRequest = { showDataDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Name") },
                                onClick = {
                                    showDataDropdown = false
                                    // Check if user is special and show appropriate popup
                                    when (userPreferencesManager.getSpecialUserType()) {
                                        "Hebron" -> {
                                            showHebronSpecialPopup = true
                                        }
                                        "Calvin" -> {
                                            showCalvinSpecialPopup = true
                                        }
                                        else -> {
                                            editingName = userName
                                            showEditNameDialog = true
                                        }
                                    }
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Load Example Data") },
                                onClick = {
                                    showDataDropdown = false
                                    DataManager.loadExampleData()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Example Data Only") },
                                onClick = {
                                    showDataDropdown = false
                                    showClearExampleConfirmDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All Data") },
                                onClick = {
                                    showDataDropdown = false
                                    showClearDataConfirmDialog = true
                                }
                            )
                        }
                    }
                    
                    IconButton(onClick = { showInstructions = !showInstructions }) {
                        Text(
                            text = "🧭",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }

        // Personalized greeting
        if (userName.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(greetingIconRes),
                            contentDescription = "Time icon",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        // Show special icons for special users - push to far end
                        // Only show if user was originally a special user (not through name editing)
                        when (userPreferencesManager.getSpecialUserType()) {
                            "Hebron" -> {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    painter = painterResource(R.drawable.ic_crown),
                                    contentDescription = "Crown",
                                    tint = Color(0xFFFFD700), // Gold color
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            "Calvin" -> {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    painter = painterResource(R.drawable.ic_racing_car),
                                    contentDescription = "Racing Car",
                                    tint = Color(0xFFFFD54F), // Yellow sports car color
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            BibleVerseCard(
                onRefresh = { /* TODO: Implement refresh */ }
            )
        }

        // Today's Tasks Section with Dropdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary // Dark green background
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp) // card radius from DayFlow
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Tasks (${todaysTasks.size})",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary // White text on dark background
                        )
                        
                            IconButton(onClick = { todaysTasksExpanded = !todaysTasksExpanded }) {
                                Icon(
                                    painter = if (todaysTasksExpanded) painterResource(R.drawable.ic_arrow_up) else painterResource(R.drawable.ic_arrow_down),
                                    contentDescription = if (todaysTasksExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onPrimary // White icons on dark background
                                )
                            }
                    }
                    
                    if (todaysTasksExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (todaysTasks.isEmpty()) {
                            Text(
                                text = "No tasks due today! 🎉",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) // Semi-transparent white
                            )
                        } else {
                            todaysTasks.forEach { task ->
                                Spacer(modifier = Modifier.height(8.dp))
                                TaskCard(
                                    title = task.title,
                                    description = task.description,
                                    isCompleted = task.isCompleted,
                                    onToggleCompletion = { /* TODO: Implement */ },
                                    onEdit = { /* TODO: Implement */ },
                                    onDelete = { /* TODO: Implement */ }
                                )
                            }
                        }
                    }
                }
            }
        }

        // This Week's Tasks Section with Dropdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ForestGreen // Forest green background
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp) // card radius from DayFlow
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tasks Due This Week (${thisWeekTasks.size})",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary // White text on dark background
                        )
                        
                            IconButton(onClick = { thisWeekTasksExpanded = !thisWeekTasksExpanded }) {
                                Icon(
                                    painter = if (thisWeekTasksExpanded) painterResource(R.drawable.ic_arrow_up) else painterResource(R.drawable.ic_arrow_down),
                                    contentDescription = if (thisWeekTasksExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onPrimary // White icons on dark background
                                )
                            }
                    }
                    
                    if (thisWeekTasksExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (thisWeekTasks.isEmpty()) {
                            Text(
                                text = "No tasks due this week! 📅",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) // Semi-transparent white
                            )
                        } else {
                            thisWeekTasks.forEach { task ->
                                Spacer(modifier = Modifier.height(8.dp))
                                TaskCard(
                                    title = task.title,
                                    description = task.description,
                                    isCompleted = task.isCompleted,
                                    onToggleCompletion = { /* TODO: Implement */ },
                                    onEdit = { /* TODO: Implement */ },
                                    onDelete = { /* TODO: Implement */ }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // lg spacing from DayFlow
                ) {
                // Task Stats
                item {
                    StatCard(
                        title = "Total Tasks",
                        value = totalTasks.toString(),
                        onClick = { onNavigateToPlanner(null) }
                    )
                }
                
                item {
                    StatCard(
                        title = "Pending Tasks",
                        value = pendingTasks.toString(),
                        onClick = { onNavigateToPlanner("pending") }
                    )
                }
                
                item {
                    StatCard(
                        title = "Completed Tasks",
                        value = completedTasks.toString(),
                        onClick = { onNavigateToPlanner("completed") }
                    )
                }
                
                // Project Stats
                item {
                    StatCard(
                        title = "Total Projects",
                        value = totalProjects.toString(),
                        onClick = { onNavigateToProjects() }
                    )
                }
                
                item {
                    StatCard(
                        title = "Active Projects",
                        value = activeProjects.toString(),
                        onClick = { onNavigateToProjects() }
                    )
                }
                
                item {
                    StatCard(
                        title = "Completed Projects",
                        value = completedProjects.toString(),
                        onClick = { onNavigateToProjects() }
                    )
                }
                
                // Debug buttons for testing persistence (temporarily disabled)
                /*
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Debug Tools",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { DataManager.addTestEntry() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Add Test")
                            }
                            
                            Button(
                                onClick = { DataManager.debugPrintState() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text("Debug")
                            }
                            
                            Button(
                                onClick = { DataManager.resetApp() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Reset")
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { DataManager.testSerialization() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Test Serial")
                            }
                            
                            Button(
                                onClick = { DataManager.testSimplePersistence() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text("Test Simple")
                            }
                            
                            Button(
                                onClick = { DataManager.manualSaveAndReloadTest() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text("Manual Test")
                            }
                        }
                    }
                }
                */
            }
        }

        // Getting Started Section - Auto-updating based on actual usage
        item {
            Text(
                text = "Getting Started",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Auto-updating Getting Started List
        val gettingStartedItems = listOf(
            GettingStartedItem("Add your first task", hasAddedTask),
            GettingStartedItem("Create a project", hasCreatedProject),
            GettingStartedItem("Log a book you're reading", hasLoggedBook),
            GettingStartedItem("Write your first journal entry", hasWrittenJournal)
        )
        
        // Only show getting started if there are incomplete items
        if (gettingStartedItems.any { !it.isCompleted }) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface // White background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp) // card radius from DayFlow
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        gettingStartedItems.forEach { item ->
                            if (!item.isCompleted) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (item.isCompleted) "✓" else "○",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Instructions Dialog
    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("Dashboard Instructions") },
            text = {
                Column {
                    Text("Welcome to your PathX Dashboard!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• View your daily inspiration quote")
                    Text("• Check quick stats about your tasks and projects")
                    Text("• See tasks due today and this week")
                    Text("• Track your progress with the Getting Started checklist")
                    Text("• Click on stats to filter tasks in the Planner")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) {
                    Text("Got it!")
                }
            }
        )
    }
    
    // Clear Example Data Only Confirmation Dialog
    if (showClearExampleConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearExampleConfirmDialog = false },
            title = {
                Text(
                    text = "Clear Example Data",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear only the example data? This will remove all sample tasks, projects, and writing entries while keeping any data you've added yourself."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        DataManager.clearExampleData()
                        showClearExampleConfirmDialog = false
                    }
                ) {
                    Text("Clear Example Data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearExampleConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear All Data Confirmation Dialog
    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            title = {
                Text(
                    text = "Clear All Data",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear ALL data? This will permanently remove all tasks, projects, and writing entries. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        DataManager.clearAllData()
                        showClearDataConfirmDialog = false
                    }
                ) {
                    Text("Clear All Data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDataConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Hebron Special Popup - Cannot edit name
    if (showHebronSpecialPopup) {
        AlertDialog(
            onDismissRequest = { showHebronSpecialPopup = false },
            title = {
                Text(
                    text = "I am sorry your honor 🧎🏾",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "A kingdom is nothing without its Queen. Among all its citizens, you stand as the most vital, the one we cannot do without. While others may change faces and names, we have but one Queen to follow, and that Queen is you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_crown),
                        contentDescription = "Crown",
                        tint = Color(0xFFFFD700), // Gold color
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showHebronSpecialPopup = false }
                ) {
                    Text("I understand, Your Majesty")
                }
            }
        )
    }
    
    // Calvin Special Popup - Cannot edit name
    if (showCalvinSpecialPopup) {
        AlertDialog(
            onDismissRequest = { showCalvinSpecialPopup = false },
            title = {
                Text(
                    text = "Nuh uh Mista not for you",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tu ne peux pas abandonner ton titre de champion Mista",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_racing_car),
                        contentDescription = "Racing Car",
                        tint = Color(0xFFFFD54F), // Yellow sports car color
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCalvinSpecialPopup = false }
                ) {
                    Text("Oui, Champion!")
                }
            }
        )
    }
    
    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = "Edit Name",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your new name:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Show warning if trying to change to special user
                    if (!userPreferencesManager.isSpecialUser() && 
                        (editingName.equals("Hebron", ignoreCase = true) || 
                         editingName.equals("Calvin", ignoreCase = true))) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Special user privileges are only available to original users",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingName.isNotBlank()) {
                            userPreferencesManager.updateUserName(editingName)
                            userName = editingName // Update local state to trigger recomposition
                            showEditNameDialog = false
                        }
                    },
                    enabled = editingName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(80.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // White background for stats
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp) // button radius from DayFlow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // lg spacing
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary // Dark green text
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface, // Dark text on white background
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class GettingStartedItem(
    val title: String,
    val isCompleted: Boolean
)

data class MockProject(
    val id: Int,
    val title: String,
    val isCompleted: Boolean,
    val todos: List<Boolean>
)