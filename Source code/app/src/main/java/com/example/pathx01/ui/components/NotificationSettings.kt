package com.example.pathx01.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettings(
    isEnabled: Boolean,
    notificationTime: LocalDateTime?,
    onEnabledChanged: (Boolean) -> Unit,
    onTimeChanged: (LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Notification Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Enable/Disable Notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Notifications",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChanged
                )
            }
            
            // Notification Time Selection
            if (isEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Notification Time",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (notificationTime != null) {
                            Text(
                                text = notificationTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = { showTimePicker = true }
                    ) {
                        Text(
                            text = if (notificationTime != null) "Change" else "Set Time"
                        )
                    }
                }
            }
            
            if (!isEnabled) {
                Text(
                    text = "Notifications will be sent based on due dates if enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        NotificationTimePickerDialog(
            initialDateTime = notificationTime,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { dateTime ->
                onTimeChanged(dateTime)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun NotificationTimePickerDialog(
    initialDateTime: LocalDateTime?,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalDateTime?) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDateTime?.toLocalDate() ?: java.time.LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(initialDateTime?.toLocalTime() ?: java.time.LocalTime.of(9, 0)) }
    var showDatePicker by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (showDatePicker) "Select Date" else "Select Time"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showDatePicker) {
                    // Date Picker
                    Text(
                        text = "Selected Date: ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Simple date navigation (you could enhance this with a proper date picker)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { selectedDate = selectedDate.minusDays(1) }
                        ) {
                            Text("← Yesterday")
                        }
                        
                        TextButton(
                            onClick = { selectedDate = selectedDate.plusDays(1) }
                        ) {
                            Text("Tomorrow →")
                        }
                    }
                } else {
                    // Time Picker
                    Text(
                        text = "Selected Time: ${selectedTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Simple time options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            java.time.LocalTime.of(8, 0) to "8:00 AM",
                            java.time.LocalTime.of(9, 0) to "9:00 AM",
                            java.time.LocalTime.of(12, 0) to "12:00 PM",
                            java.time.LocalTime.of(15, 0) to "3:00 PM",
                            java.time.LocalTime.of(18, 0) to "6:00 PM",
                            java.time.LocalTime.of(20, 0) to "8:00 PM"
                        ).forEach { (time, label) ->
                            FilterChip(
                                selected = selectedTime == time,
                                onClick = { selectedTime = time },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (showDatePicker) {
                        showDatePicker = false
                    } else {
                        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                        onTimeSelected(dateTime)
                    }
                }
            ) {
                Text(if (showDatePicker) "Next" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
