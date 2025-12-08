package com.example.pathx01.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pathx01.MainActivity
import com.example.pathx01.R
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.Project
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PathXNotificationManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID_TASKS = "task_notifications"
        const val CHANNEL_ID_PROJECTS = "project_notifications"
        const val CHANNEL_ID_GENERAL = "general_notifications"
        
        const val NOTIFICATION_ID_TASK_DUE = 1001
        const val NOTIFICATION_ID_TASK_OVERDUE = 1002
        const val NOTIFICATION_ID_TASK_REMINDER = 1003
        const val NOTIFICATION_ID_PROJECT_DUE = 2001
        const val NOTIFICATION_ID_DAILY_REMINDER = 3001
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Task Notifications Channel
            val taskChannel = NotificationChannel(
                CHANNEL_ID_TASKS,
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task due dates and reminders"
                enableVibration(true)
                enableLights(true)
            }
            
            // Project Notifications Channel
            val projectChannel = NotificationChannel(
                CHANNEL_ID_PROJECTS,
                "Project Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for project deadlines and updates"
                enableVibration(true)
            }
            
            // General Notifications Channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications and reminders"
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannels(listOf(taskChannel, projectChannel, generalChannel))
        }
    }
    
    fun showTaskDueNotification(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "planner")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("Task Due Today")
            .setContentText("${task.title} is due today")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Don't forget to complete '${task.title}' today. ${if (task.description?.isNotBlank() == true) "\n\n${task.description}" else ""}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Mark Complete",
                createMarkCompletePendingIntent(task.id)
            )
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TASK_DUE + task.id, notification)
    }
    
    fun showTaskOverdueNotification(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "planner")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDateTime.parse(task.dueDate!!),
            LocalDateTime.now()
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
            .setSmallIcon(R.drawable.ic_mood_anxious)
            .setContentTitle("Task Overdue")
            .setContentText("${task.title} is ${daysOverdue} day(s) overdue")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("'${task.title}' was due ${daysOverdue} day(s) ago. Please complete it as soon as possible. ${if (task.description?.isNotBlank() == true) "\n\n${task.description}" else ""}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Mark Complete",
                createMarkCompletePendingIntent(task.id)
            )
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TASK_OVERDUE + task.id, notification)
    }
    
    fun showTaskReminderNotification(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "planner")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val markCompleteIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = "MARK_COMPLETE"
            putExtra("task_id", task.id)
        }
        
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
            .setSmallIcon(R.drawable.ic_list)
            .setContentTitle("Task Reminder")
            .setContentText("Don't forget: ${task.title}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Don't forget: ${task.title}\n${task.description ?: ""}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Mark Complete",
                markCompletePendingIntent
            )
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TASK_REMINDER + task.id, notification)
    }
    
    fun showProjectDueNotification(project: Project) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "projects")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            project.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROJECTS)
            .setSmallIcon(R.drawable.ic_list)
            .setContentTitle("Project Due Soon")
            .setContentText("${project.title} deadline is approaching")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Project '${project.title}' deadline is coming up. Make sure you're on track to meet your goals. ${if (project.description.isNotBlank()) "\n\n${project.description}" else ""}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PROJECT_DUE + project.id, notification)
    }
    
    fun showDailyReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DAILY_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_sun)
            .setContentTitle("Good Morning!")
            .setContentText("Check your tasks and stay productive today")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Start your day by checking your tasks and goals. Stay focused and achieve your objectives!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY_REMINDER, notification)
    }
    
    private fun createMarkCompletePendingIntent(taskId: Int): PendingIntent {
        val intent = Intent(context, TaskActionReceiver::class.java).apply {
            action = "MARK_COMPLETE"
            putExtra("task_id", taskId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
