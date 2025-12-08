package com.example.pathx01.notifications

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.Project
import java.time.LocalDateTime

object NotificationService {
    
    private var notificationManager: PathXNotificationManager? = null
    private var notificationScheduler: NotificationScheduler? = null
    
    fun initialize(context: Context) {
        notificationManager = PathXNotificationManager(context)
        notificationScheduler = NotificationScheduler(context)
        
        // Schedule daily reminder
        notificationScheduler?.scheduleDailyReminder()
        
        // Reschedule all existing notifications to ensure they're properly set up
        rescheduleAllNotifications(context)
    }
    
    private fun rescheduleAllNotifications(context: Context) {
        // Reschedule all task notifications
        com.example.pathx01.data.DataManager.getTasks()
            .filter { !it.isCompleted && it.dueDate != null }
            .forEach { task ->
                scheduleTaskNotifications(task, context)
            }
        
        // Reschedule all project notifications
        com.example.pathx01.data.DataManager.getProjects()
            .forEach { project ->
                scheduleProjectNotifications(project, context)
            }
    }
    
    fun scheduleTaskNotifications(task: Task, context: Context) {
        if (notificationScheduler == null) {
            initialize(context)
        }
        notificationScheduler?.scheduleTaskNotifications(task)
    }
    
    fun scheduleTaskNotificationAtTime(task: Task, notificationTime: LocalDateTime, context: Context) {
        if (notificationScheduler == null) {
            initialize(context)
        }
        notificationScheduler?.scheduleTaskNotificationAtTime(task, notificationTime)
    }
    
    fun scheduleProjectNotifications(project: Project, context: Context) {
        if (notificationScheduler == null) {
            initialize(context)
        }
        notificationScheduler?.scheduleProjectNotifications(project)
    }
    
    fun cancelTaskNotifications(taskId: Int, context: Context) {
        if (notificationScheduler == null) {
            initialize(context)
        }
        notificationScheduler?.cancelTaskNotifications(taskId)
    }
    
    fun cancelProjectNotifications(projectId: Int, context: Context) {
        if (notificationScheduler == null) {
            initialize(context)
        }
        notificationScheduler?.cancelProjectNotifications(projectId)
    }
    
    fun checkAndShowImmediateNotifications(context: Context) {
        if (notificationManager == null) {
            initialize(context)
        }
        
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        
        // Check for tasks due today or overdue
        com.example.pathx01.data.DataManager.getTasks()
            .filter { !it.isCompleted && it.dueDate != null }
            .forEach { task ->
                val dueDate = LocalDateTime.parse(task.dueDate!!)
                val taskDueDate = dueDate.toLocalDate()
                
                when {
                    taskDueDate.isEqual(today) -> {
                        notificationManager?.showTaskDueNotification(task)
                    }
                    taskDueDate.isBefore(today) -> {
                        notificationManager?.showTaskOverdueNotification(task)
                    }
                }
            }
        
        // Check for projects that need reminders (7 days after creation)
        com.example.pathx01.data.DataManager.getProjects()
            .forEach { project ->
                val daysSinceCreation = java.time.temporal.ChronoUnit.DAYS.between(project.createdAt.toLocalDate(), today)
                
                if (daysSinceCreation >= 7) {
                    notificationManager?.showProjectDueNotification(project)
                }
            }
    }
    
    fun requestNotificationPermission(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Request notification permission for Android 13+
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                
                // This would typically be called from an Activity with requestPermissions
                // For now, we'll just initialize the notification system
                initialize(context)
            }
        } else {
            initialize(context)
        }
    }
    
    // Debug method to test notifications immediately
    fun testNotificationNow(context: Context) {
        if (notificationManager == null) {
            initialize(context)
        }
        
        // Create a test task
        val testTask = com.example.pathx01.data.model.Task(
            id = 999,
            title = "Test Notification",
            description = "This is a test notification to verify the system works",
            category = "Test",
            priority = com.example.pathx01.data.model.Priority.HIGH,
            isCompleted = false
        )
        
        // Show notification immediately
        notificationManager?.showTaskReminderNotification(testTask)
        
        android.util.Log.d("NotificationService", "Test notification sent immediately")
    }
}
