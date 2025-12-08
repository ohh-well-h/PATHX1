package com.example.pathx01.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.pathx01.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.pathx01.data.DataManager
import com.example.pathx01.data.Project
import com.example.pathx01.data.ProjectTodo
import com.example.pathx01.notifications.NotificationService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen() {
    val context = LocalContext.current
    val projects = DataManager.getProjects()
    
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showEditProjectDialog by remember { mutableStateOf(false) }
    var showManageTodosDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<Project?>(null) }
    var managingProject by remember { mutableStateOf<Project?>(null) }
    var showInstructions by remember { mutableStateOf(false) }
    var deletingProject by remember { mutableStateOf<Project?>(null) }

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
                    text = "Projects",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
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
                        onClick = { showAddProjectDialog = true },
                        modifier = Modifier.size(56.dp), // FAB size from DayFlow design system
                        containerColor = MaterialTheme.colorScheme.secondary, // Gold color
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp) // FAB radius from DayFlow
                    ) {
                        Icon(painterResource(R.drawable.ic_add), "Add Project")
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Active Projects (${projects.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (projects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🚀 No projects yet!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first project to start tracking your progress!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(projects) { project ->
                ProjectCard(
                    project = project,
                    onEdit = { 
                        editingProject = project
                        showEditProjectDialog = true
                    },
                    onDelete = { 
                        deletingProject = project
                        showDeleteConfirmDialog = true
                    },
                    onManageTodos = { 
                        managingProject = project
                        showManageTodosDialog = true
                    }
                )
            }
        }
    }
    
    // Add Project Dialog
    if (showAddProjectDialog) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onAddProject = { title, description, todos ->
                val newProject = Project(
                    id = 0, // DataManager will assign the correct ID
                    title = title,
                    description = description,
                    createdAt = LocalDateTime.now(),
                    todos = todos
                )
                DataManager.addProject(newProject)
                
                // Schedule notifications for new project
                NotificationService.scheduleProjectNotifications(newProject, context)
                
                showAddProjectDialog = false
            }
        )
    }
    
    // Edit Project Dialog
    if (showEditProjectDialog && editingProject != null) {
        EditProjectDialog(
            project = editingProject!!,
            onDismiss = { 
                showEditProjectDialog = false
                editingProject = null
            },
            onUpdateProject = { updatedProject ->
                DataManager.updateProject(updatedProject)
                
                // Reschedule notifications for updated project
                NotificationService.cancelProjectNotifications(updatedProject.id, context)
                NotificationService.scheduleProjectNotifications(updatedProject, context)
                
                showEditProjectDialog = false
                editingProject = null
            }
        )
    }
    
    // Manage Todos Dialog
    if (showManageTodosDialog && managingProject != null) {
        ManageTodosDialog(
            project = managingProject!!,
            onDismiss = { 
                showManageTodosDialog = false
                managingProject = null
            },
            onUpdateTodos = { updatedTodos ->
                managingProject?.let { project ->
                    val updatedProject = project.copy(todos = updatedTodos)
                    DataManager.updateProject(updatedProject)
                }
                showManageTodosDialog = false
                managingProject = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && deletingProject != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false
                deletingProject = null
            },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete '${deletingProject?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingProject?.let { project ->
                            DataManager.deleteProject(project.id)
                            // Cancel notifications for deleted project
                            NotificationService.cancelProjectNotifications(project.id, context)
                        }
                        showDeleteConfirmDialog = false
                        deletingProject = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmDialog = false
                        deletingProject = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Instructions Dialog
    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            title = { Text("Projects Instructions") },
            text = {
                Column {
                    Text("Welcome to your Projects section!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Tap + to create new projects")
                    Text("• Each project can have multiple todo items")
                    Text("• Progress is tracked automatically")
                    Text("• Tap edit to modify project details")
                    Text("• Tap manage todos to update progress")
                    Text("• Tap delete to remove projects")
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
fun ProjectCard(
    project: Project,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageTodos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedTodos = project.todos.count { it.isCompleted }
    val totalTodos = project.todos.size
    val progress = if (totalTodos > 0) completedTodos.toFloat() / totalTodos.toFloat() else 0f
    val progressPercentage = (progress * 100).toInt()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Created: ${project.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(painterResource(R.drawable.ic_edit), "Edit Project")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(painterResource(R.drawable.ic_delete), "Delete Project", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress: $completedTodos/$totalTodos tasks ($progressPercentage%)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                TextButton(onClick = onManageTodos) {
                    Text("Manage Todos")
                }
            }
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            
            // Quick Todo Preview
            if (project.todos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Todos:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                project.todos.take(3).forEach { todo ->
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = todo.isCompleted,
                            onCheckedChange = { /* Handled in manage todos dialog */ }
                        )
                        Text(
                            text = todo.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (todo.isCompleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
                
                if (project.todos.size > 3) {
                    Text(
                        text = "... and ${project.todos.size - 3} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onAddProject: (String, String, List<ProjectTodo>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var todos by remember { mutableStateOf(listOf<ProjectTodo>()) }
    var newTodoText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Project") },
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
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // Todo List Section
                Text(
                    text = "Project Todos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Add Todo Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTodoText,
                        onValueChange = { newTodoText = it },
                        label = { Text("Add todo item") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = {
                            if (newTodoText.isNotBlank()) {
                                todos = todos + ProjectTodo(newTodoText.trim(), false)
                                newTodoText = ""
                            }
                        }
                    ) {
                        Icon(painterResource(R.drawable.ic_add), "Add Todo")
                    }
                }
                
                // Display Todos
                if (todos.isNotEmpty()) {
                    Text(
                        text = "Todo Items:",
                        style = MaterialTheme.typography.labelSmall
                    )
                    
                    todos.forEachIndexed { index, todo ->
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = todo.isCompleted,
                                onCheckedChange = { checked ->
                                    todos = todos.mapIndexed { i, t ->
                                        if (i == index) t.copy(isCompleted = checked) else t
                                    }
                                }
                            )
                            
                            Text(
                                text = todo.text,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            IconButton(
                                onClick = { todos = todos.filterIndexed { i, _ -> i != index } }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_delete),
                                    "Remove Todo",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddProject(title, description, todos)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Project")
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
fun EditProjectDialog(
    project: Project,
    onDismiss: () -> Unit,
    onUpdateProject: (Project) -> Unit
) {
    var title by remember { mutableStateOf(project.title) }
    var description by remember { mutableStateOf(project.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Project") },
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
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val updatedProject = project.copy(
                            title = title,
                            description = description
                        )
                        onUpdateProject(updatedProject)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Update Project")
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
fun ManageTodosDialog(
    project: Project,
    onDismiss: () -> Unit,
    onUpdateTodos: (List<ProjectTodo>) -> Unit
) {
    var todos by remember { mutableStateOf(project.todos) }
    var newTodoText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Todos - ${project.title}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Summary
                val completed = todos.count { it.isCompleted }
                val total = todos.size
                val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
                
                Text(
                    text = "Progress: $completed/$total (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Add New Todo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTodoText,
                        onValueChange = { newTodoText = it },
                        label = { Text("Add todo item") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = {
                            if (newTodoText.isNotBlank()) {
                                todos = todos + ProjectTodo(newTodoText.trim(), false)
                                newTodoText = ""
                            }
                        }
                    ) {
                        Icon(painterResource(R.drawable.ic_add), "Add Todo")
                    }
                }
                
                // Todo List
                if (todos.isNotEmpty()) {
                    Text(
                        text = "Todo Items:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    todos.forEachIndexed { index, todo ->
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = todo.isCompleted,
                                onCheckedChange = { checked ->
                                    todos = todos.mapIndexed { i, t ->
                                        if (i == index) t.copy(isCompleted = checked) else t
                                    }
                                }
                            )
                            
                            Text(
                                text = todo.text,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (todo.isCompleted) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            
                            IconButton(
                                onClick = { todos = todos.filterIndexed { i, _ -> i != index } }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_delete),
                                    "Remove Todo",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdateTodos(todos)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
