package com.example.pathx01.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.pathx01.data.model.Subtask
import com.example.pathx01.utils.SubtaskUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtaskManager(
    subtasks: List<Subtask>,
    onSubtasksChanged: (List<Subtask>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSubtask by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtasks (${subtasks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            IconButton(
                onClick = { showAddSubtask = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subtask",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Subtasks List
        if (subtasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "No subtasks yet. Tap + to add one!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(subtasks) { subtask ->
                    SubtaskItem(
                        subtask = subtask,
                        onToggleCompletion = { isCompleted ->
                            val updatedSubtasks = SubtaskUtils.updateSubtask(
                                subtasks, 
                                subtask.id, 
                                isCompleted = isCompleted
                            )
                            onSubtasksChanged(updatedSubtasks)
                        },
                        onDelete = {
                            val updatedSubtasks = SubtaskUtils.deleteSubtask(subtasks, subtask.id)
                            onSubtasksChanged(updatedSubtasks)
                        },
                        onEdit = { newText ->
                            val updatedSubtasks = SubtaskUtils.updateSubtask(
                                subtasks, 
                                subtask.id, 
                                text = newText
                            )
                            onSubtasksChanged(updatedSubtasks)
                        }
                    )
                }
            }
        }
        
        // Add Subtask Dialog
        if (showAddSubtask) {
            AlertDialog(
                onDismissRequest = { 
                    showAddSubtask = false
                    newSubtaskText = ""
                },
                title = { Text("Add Subtask") },
                text = {
                    OutlinedTextField(
                        value = newSubtaskText,
                        onValueChange = { newSubtaskText = it },
                        label = { Text("Subtask description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newSubtaskText.isNotBlank()) {
                                val newSubtask = SubtaskUtils.createSubtask(newSubtaskText)
                                onSubtasksChanged(subtasks + newSubtask)
                                showAddSubtask = false
                                newSubtaskText = ""
                            }
                        },
                        enabled = newSubtaskText.isNotBlank()
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showAddSubtask = false
                            newSubtaskText = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SubtaskItem(
    subtask: Subtask,
    onToggleCompletion: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(subtask.text) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (subtask.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Completion Checkbox
            Checkbox(
                checked = subtask.isCompleted,
                onCheckedChange = onToggleCompletion,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            // Text or Edit Field
            if (isEditing) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { newText -> editText = newText },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (editText.isNotBlank()) {
                                onEdit(editText)
                                isEditing = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Text(
                    text = subtask.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEditing = true },
                    color = if (subtask.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (subtask.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
            }
            
            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Subtask",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
