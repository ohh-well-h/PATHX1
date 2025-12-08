package com.example.pathx01.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.pathx01.R
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.model.Priority
import com.example.pathx01.data.DataManager
import com.example.pathx01.notifications.NotificationService
import com.example.pathx01.ui.components.SubtaskManager
import com.example.pathx01.ui.components.NotificationSettingsPopup
import com.example.pathx01.utils.SubtaskUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import android.widget.DatePicker
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    filter: String? = null,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    // Use shared data from DataManager
    val tasks = DataManager.getTasks()
    
    val categories = remember { 
        mutableStateOf(listOf("All", "Academic", "Test Prep", "College Apps", "Personal", "Reading", "Projects"))
    }
    
    var selectedCategory by remember { mutableStateOf(
        when (filter) {
            "pending" -> "All"
            "completed" -> "All"
            "projects" -> "All"
            else -> "All"
        }
    ) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showEditTaskDialog by remember { mutableStateOf(false) }
    var showViewTaskDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCompletionNotification by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var viewingTask by remember { mutableStateOf<Task?>(null) }
    var deletingTask by remember { mutableStateOf<Task?>(null) }
        var completedTask by remember { mutableStateOf<Task?>(null) }
        var notificationDismissed by remember { mutableStateOf(false) }
        var showInstructions by remember { mutableStateOf(false) }
    
    val filteredTasks = when {
        filter == "pending" -> tasks.filter { !it.isCompleted }
        filter == "completed" -> tasks.filter { it.isCompleted }
        filter == "projects" -> tasks.filter { it.category == "Projects" }
        selectedCategory == "All" -> tasks
        else -> tasks.filter { it.category == selectedCategory }
    }

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
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Show back button if navigated from stats
                    if (filter != null) {
                        IconButton(
                            onClick = { 
                                onNavigateBack?.invoke()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_left), // Back arrow
                                contentDescription = "Back to Dashboard"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Task Planner",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showInstructions = !showInstructions }) {
                            Text(
                                text = "🧭",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    
                    FloatingActionButton(
                        onClick = { showAddTaskDialog = true },
                        modifier = Modifier.size(56.dp), // FAB size from DayFlow design system
                        containerColor = MaterialTheme.colorScheme.secondary, // Gold color
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp) // FAB radius from DayFlow
                    ) {
                            Icon(painterResource(R.drawable.ic_add), "Add Task")
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Filter by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.value) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
                
                item {
                    AssistChip(
                        onClick = { showCategoryDialog = true },
                        label = { Text("+ Add Category") }
                    )
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks (${filteredTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Due Date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (filteredTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (selectedCategory == "All") {
                            "No tasks yet. Add your first task! 📝"
                        } else {
                            "No tasks in '$selectedCategory' category."
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredTasks) { task ->
                TaskCard(
                    task = task,
                    onToggleComplete = { 
                        completedTask = task
                        showCompletionNotification = true
                        DataManager.toggleTaskCompletion(task.id)
                        
                        // Cancel notifications for completed task
                        NotificationService.cancelTaskNotifications(task.id, context)
                    },
                    onEdit = { 
                        editingTask = task
                        showEditTaskDialog = true
                    },
                    onDelete = { 
                        deletingTask = task
                        showDeleteConfirmDialog = true
                    },
                    onView = {
                        viewingTask = task
                        showViewTaskDialog = true
                    }
                )
            }
        }
    }
    
    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            categories = categories.value.drop(1), // Remove "All"
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, description, category, priority, dueDate, subtasks, notificationsEnabled, notificationTime ->
                val subtasksJson = SubtaskUtils.subtasksToJson(subtasks)
                val newTask = Task(
                    id = 0, // DataManager will assign the correct ID
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate?.toString(),
                    subtasks = subtasksJson
                )
                DataManager.addTask(newTask)
                
                // Schedule notifications for new task if enabled
                if (notificationsEnabled && notificationTime != null) {
                    NotificationService.scheduleTaskNotificationAtTime(newTask, notificationTime, context)
                } else {
                    // Use default notification scheduling if no custom time set
                    NotificationService.scheduleTaskNotifications(newTask, context)
                }
                
                if (category !in categories.value) {
                    categories.value = categories.value + category
                }
                showAddTaskDialog = false
            }
        )
    }
    
    // Edit Task Dialog
    if (showEditTaskDialog && editingTask != null) {
        EditTaskDialog(
            task = editingTask!!,
            categories = categories.value.drop(1),
            onDismiss = { 
                showEditTaskDialog = false
                editingTask = null
            },
            onUpdateTask = { updatedTask, notificationsEnabled, notificationTime ->
                DataManager.updateTask(updatedTask)
                
                // Reschedule notifications for updated task
                NotificationService.cancelTaskNotifications(updatedTask.id, context)
                if (notificationsEnabled && notificationTime != null) {
                    NotificationService.scheduleTaskNotificationAtTime(updatedTask, notificationTime, context)
                } else {
                    // Use default notification scheduling if no custom time set
                    NotificationService.scheduleTaskNotifications(updatedTask, context)
                }
                
                showEditTaskDialog = false
                editingTask = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && deletingTask != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false
                deletingTask = null
            },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${deletingTask?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingTask?.let { task ->
                            DataManager.deleteTask(task.id)
                            // Cancel notifications for deleted task
                            NotificationService.cancelTaskNotifications(task.id, context)
                        }
                        showDeleteConfirmDialog = false
                        deletingTask = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmDialog = false
                        deletingTask = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Add Category Dialog
    if (showCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onAddCategory = { categoryName ->
                if (categoryName !in categories.value) {
                    categories.value = categories.value + categoryName
                }
                showCategoryDialog = false
            }
        )
    }
    
    // Completion Notification
    if (showCompletionNotification && completedTask != null && !notificationDismissed) {
        CompletionNotificationDialog(
            task = completedTask!!,
            onDismiss = { 
                showCompletionNotification = false
                completedTask = null
                notificationDismissed = false
            },
            onDoNotRepeat = {
                showCompletionNotification = false
                completedTask = null
                notificationDismissed = true
                // Task will disappear in 3 hours
            }
        )
    }
    
    // View Task Dialog
    if (showViewTaskDialog && viewingTask != null) {
        ViewTaskDialog(
            task = viewingTask!!,
            onDismiss = { 
                showViewTaskDialog = false
                viewingTask = null
            },
            onEdit = {
                showViewTaskDialog = false
                editingTask = viewingTask
                showEditTaskDialog = true
                viewingTask = null
            }
        )
    }
    
    // Instructions Dialog
    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("Task Planner Instructions") },
            text = {
                Column {
                    Text("Welcome to your Task Planner!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Filter tasks by category using the chips above")
                    Text("• Tap + to add new tasks with dates and priorities")
                    Text("• Tap the checkbox to complete tasks")
                    Text("• Tap edit to modify task details")
                    Text("• Tap delete to remove tasks")
                    Text("• Create custom categories as needed")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit = {},
    modifier: Modifier = Modifier
) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onView() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), // card radius from DayFlow
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface // White background
            )
        ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (!task.description.isNullOrEmpty()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (task.isCompleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                            Icon(painterResource(R.drawable.ic_edit), "Edit Task")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(painterResource(R.drawable.ic_delete), "Delete Task", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(task.category) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text(task.priority.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (task.priority) {
                            Priority.URGENT -> MaterialTheme.colorScheme.errorContainer
                            Priority.HIGH -> MaterialTheme.colorScheme.primaryContainer
                            Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                            Priority.LOW -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )
                )
            }
            
            if (!task.dueDate.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = "Due Date",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(task.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Display subtasks if any
            val taskSubtasks = SubtaskUtils.jsonToSubtasks(task.subtasks)
            if (taskSubtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_list),
                        contentDescription = "Subtasks",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val completedCount = taskSubtasks.count { it.isCompleted }
                    Text(
                        text = "Subtasks: $completedCount/${taskSubtasks.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAddTask: (String, String?, String, Priority, LocalDateTime?, List<com.example.pathx01.data.model.Subtask>, Boolean, LocalDateTime?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var showCategoryInput by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var subtasks by remember { mutableStateOf<List<com.example.pathx01.data.model.Subtask>>(emptyList()) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationTime by remember { mutableStateOf<LocalDateTime?>(null) }
    
    val context = LocalContext.current
    
    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            val datePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                    selectedDate = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDayOfMonth, 0, 0)
                    showDatePicker = false
                }, year, month, day
            )
            datePickerDialog.show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Limit height for scrollability
                    .verticalScroll(rememberScrollState()), // Make it scrollable
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // Category Selection with Dropdown
                var categoryDropdownExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = { selectedCategory = it },
                            label = { Text("Category") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .weight(1f)
                                .menuAnchor()
                        )
                        
                        TextButton(
                            onClick = { showCategoryInput = true }
                        ) {
                            Text("+ New")
                        }
                    }
                    
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // New Category Input
                if (showCategoryInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("New Category") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        TextButton(
                            onClick = { 
                                if (newCategoryName.isNotBlank()) {
                                    selectedCategory = newCategoryName
                                    showCategoryInput = false
                                    newCategoryName = ""
                                }
                            }
                        ) {
                            Text("Add")
                        }
                        
                        TextButton(
                            onClick = { showCategoryInput = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
                
                // Priority Selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Priority.values().toList()) { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name) }
                        )
                    }
                }
                
                // Due Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    TextButton(
                        onClick = { showDatePicker = true }
                    ) {
                        Text(
                            text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Select Date"
                        )
                    }
                }
                
                // Subtasks
                SubtaskManager(
                    subtasks = subtasks,
                    onSubtasksChanged = { newSubtasks ->
                        subtasks = newSubtasks
                    }
                )
                
                // Notification Settings Button
                Button(
                    onClick = { showNotificationSettings = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification Settings",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notification Settings")
                }
                
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, description, selectedCategory, selectedPriority, selectedDate, subtasks, notificationsEnabled, notificationTime)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Notification Settings Popup
    NotificationSettingsPopup(
        isOpen = showNotificationSettings,
        onDismiss = { showNotificationSettings = false },
        onSave = { enabled, time ->
            notificationsEnabled = enabled
            notificationTime = time
        }
    )
}

@Composable
fun EditTaskDialog(
    task: Task,
    categories: List<String>,
    onDismiss: () -> Unit,
    onUpdateTask: (Task, Boolean, LocalDateTime?) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description ?: "") }
    var selectedCategory by remember { mutableStateOf(task.category) }
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var selectedDate by remember { mutableStateOf(
        task.dueDate?.let { 
            try {
                LocalDateTime.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    ) }
    var showEditDatePicker by remember { mutableStateOf(false) }
    var subtasks by remember { mutableStateOf(SubtaskUtils.jsonToSubtasks(task.subtasks)) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationTime by remember { mutableStateOf<LocalDateTime?>(null) }
    
    val context = LocalContext.current
    
    LaunchedEffect(showEditDatePicker) {
        if (showEditDatePicker) {
            val calendar = Calendar.getInstance()
            val year = selectedDate?.year ?: calendar.get(Calendar.YEAR)
            val month = selectedDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
            val day = selectedDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
            
            val datePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                    selectedDate = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDayOfMonth, 0, 0)
                    showEditDatePicker = false
                }, year, month, day
            )
            datePickerDialog.show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Limit height for scrollability
                    .verticalScroll(rememberScrollState()), // Make it scrollable
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { selectedCategory = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Priority Selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Priority.values().toList()) { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name) }
                        )
                    }
                }
                
                // Due Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    TextButton(
                        onClick = { showEditDatePicker = true }
                    ) {
                        Text(
                            text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Select Date"
                        )
                    }
                }
                
                // Subtasks
                SubtaskManager(
                    subtasks = subtasks,
                    onSubtasksChanged = { newSubtasks ->
                        subtasks = newSubtasks
                    }
                )
                
                // Notification Settings Button
                Button(
                    onClick = { showNotificationSettings = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification Settings",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notification Settings")
                }
                
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val subtasksJson = SubtaskUtils.subtasksToJson(subtasks)
                        val updatedTask = task.copy(
                            title = title,
                            description = description.ifBlank { null },
                            category = selectedCategory,
                            priority = selectedPriority,
                            dueDate = selectedDate?.toString(),
                            subtasks = subtasksJson
                        )
                        onUpdateTask(updatedTask, notificationsEnabled, notificationTime)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Update Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Notification Settings Popup
    NotificationSettingsPopup(
        isOpen = showNotificationSettings,
        onDismiss = { showNotificationSettings = false },
        onSave = { enabled, time ->
            notificationsEnabled = enabled
            notificationTime = time
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onAddCategory(categoryName.trim())
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Add Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CompletionNotificationDialog(
    task: Task,
    onDismiss: () -> Unit,
    onDoNotRepeat: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Task Completed! 🎉") },
        text = {
            Text("This task will disappear in 3 hours. You can complete it now or let it auto-remove later.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDoNotRepeat) {
                Text("Don't Repeat")
            }
        }
    )
}

@Composable
fun ViewTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Task Details") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Title: ${task.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                task.description?.let { description ->
                    Text(
                        text = "Description: $description",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Text(
                    text = "Category: ${task.category}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Priority: ${task.priority.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Status: ${if (task.isCompleted) "Completed" else "Pending"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                task.dueDate?.let { dueDate ->
                    Text(
                        text = "Due Date: $dueDate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDateTime.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        "Invalid Date"
    }
}