package com.example.pathx01.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsPopup(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationTime by remember { mutableStateOf<LocalDateTime?>(null) }
    val context = LocalContext.current

    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Notification Settings") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Enable/Disable Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(24.dp),
                                tint = if (notificationsEnabled) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Notifications",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }

                    // Time Selection (only if notifications are enabled)
                    if (notificationsEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Notification Time",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    val hour = notificationTime?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
                                    val minute = notificationTime?.minute ?: calendar.get(Calendar.MINUTE)
                                    
                                    TimePickerDialog(
                                        context,
                                        { _, selectedHour, selectedMinute ->
                                            val now = LocalDateTime.now()
                                            notificationTime = now.withHour(selectedHour).withMinute(selectedMinute).withSecond(0).withNano(0)
                                        }, 
                                        hour, 
                                        minute, 
                                        false
                                    ).show()
                                }
                            ) {
                                Text(
                                    text = notificationTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Select Time"
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(notificationsEnabled, notificationTime)
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
