package com.example.pathx01.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.pathx01.data.DataManager

class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received intent with action: ${intent.action}")
        
        when (intent.action) {
            "SHOW_NOTIFICATION" -> {
                val notificationId = intent.getIntExtra("notification_id", -1)
                val title = intent.getStringExtra("title") ?: ""
                val message = intent.getStringExtra("message") ?: ""
                val taskId = intent.getIntExtra("task_id", -1)
                
                Log.d("NotificationReceiver", "SHOW_NOTIFICATION: id=$notificationId, title=$title, taskId=$taskId")
                
                if (notificationId != -1 && title.isNotEmpty() && message.isNotEmpty()) {
                    val notificationManager = PathXNotificationManager(context)
                    
                    // Find the task/project to show detailed notification
                    val task = DataManager.getTasks().find { it.id == taskId }
                    task?.let {
                        when {
                            title.contains("Due Today") -> notificationManager.showTaskDueNotification(it)
                            title.contains("Overdue") -> notificationManager.showTaskOverdueNotification(it)
                            title.contains("Task Reminder") -> notificationManager.showTaskReminderNotification(it)
                        }
                    }
                    
                    // If not a task, try to find project
                    if (task == null) {
                        val project = DataManager.getProjects().find { it.id == taskId }
                        project?.let {
                            if (title.contains("Project")) {
                                notificationManager.showProjectDueNotification(it)
                            }
                        }
                    }
                }
            }
            
            "DAILY_REMINDER" -> {
                Log.d("NotificationReceiver", "DAILY_REMINDER triggered")
                val notificationManager = PathXNotificationManager(context)
                notificationManager.showDailyReminderNotification()
            }
        }
    }
}
