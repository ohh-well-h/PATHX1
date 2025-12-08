package com.example.pathx01.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.pathx01.data.model.Task
import com.example.pathx01.data.Project
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

class NotificationScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = PathXNotificationManager(context)
    
    fun scheduleTaskNotifications(task: Task) {
        if (task.dueDate == null || task.isCompleted) return
        
        val dueDate = LocalDateTime.parse(task.dueDate!!)
        
        // Schedule notification for due date (9 AM on due date)
        scheduleNotification(
            taskId = task.id,
            title = "Task Due Today",
            message = "${task.title} is due today",
            notificationTime = dueDate.toLocalDate().atTime(9, 0),
            notificationId = PathXNotificationManager.NOTIFICATION_ID_TASK_DUE + task.id
        )
        
        // Schedule overdue notification (9 AM the day after due date)
        val overdueDate = dueDate.plusDays(1).toLocalDate().atTime(9, 0)
        scheduleNotification(
            taskId = task.id,
            title = "Task Overdue",
            message = "${task.title} is overdue",
            notificationTime = overdueDate,
            notificationId = PathXNotificationManager.NOTIFICATION_ID_TASK_OVERDUE + task.id
        )
    }
    
    fun scheduleTaskNotificationAtTime(task: Task, notificationTime: LocalDateTime) {
        if (task.isCompleted) return
        
        android.util.Log.d("NotificationScheduler", "Scheduling notification for task '${task.title}' at $notificationTime")
        
        // Schedule custom notification at the specified time
        scheduleNotification(
            taskId = task.id,
            title = "Task Reminder",
            message = "Don't forget: ${task.title}",
            notificationTime = notificationTime,
            notificationId = PathXNotificationManager.NOTIFICATION_ID_TASK_REMINDER + task.id
        )
    }
    
    fun scheduleProjectNotifications(project: Project) {
        // Schedule notification 7 days after project creation as a reminder
        val reminderDate = project.createdAt.plusDays(7).withHour(10).withMinute(0)
        scheduleNotification(
            taskId = project.id,
            title = "Project Reminder",
            message = "Don't forget to check on your project: ${project.title}",
            notificationTime = reminderDate,
            notificationId = PathXNotificationManager.NOTIFICATION_ID_PROJECT_DUE + project.id
        )
    }
    
    fun scheduleDailyReminder() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8) // 8 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If it's already past 8 AM today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "DAILY_REMINDER"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PathXNotificationManager.NOTIFICATION_ID_DAILY_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Use setExactAndAllowWhileIdle for better reliability on newer Android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
    
    private fun scheduleNotification(
        taskId: Int,
        title: String,
        message: String,
        notificationTime: LocalDateTime,
        notificationId: Int
    ) {
        val calendar = Calendar.getInstance().apply {
            val instant = notificationTime.atZone(ZoneId.systemDefault()).toInstant()
            timeInMillis = instant.toEpochMilli()
        }
        
        val currentTime = System.currentTimeMillis()
        val scheduledTime = calendar.timeInMillis
        val delayMinutes = (scheduledTime - currentTime) / (1000 * 60)
        
        android.util.Log.d("NotificationScheduler", 
            "Scheduling notification '$title' for task $taskId\n" +
            "Current time: ${java.util.Date(currentTime)}\n" +
            "Scheduled time: ${java.util.Date(scheduledTime)}\n" +
            "Delay: $delayMinutes minutes\n" +
            "Notification ID: $notificationId"
        )
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "SHOW_NOTIFICATION"
            putExtra("notification_id", notificationId)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("task_id", taskId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Use setExactAndAllowWhileIdle for better reliability on newer Android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    fun cancelTaskNotifications(taskId: Int) {
        val intent1 = Intent(context, NotificationReceiver::class.java)
        val intent2 = Intent(context, NotificationReceiver::class.java)
        
        val pendingIntent1 = PendingIntent.getBroadcast(
            context,
            PathXNotificationManager.NOTIFICATION_ID_TASK_DUE + taskId,
            intent1,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val pendingIntent2 = PendingIntent.getBroadcast(
            context,
            PathXNotificationManager.NOTIFICATION_ID_TASK_OVERDUE + taskId,
            intent2,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent1)
        alarmManager.cancel(pendingIntent2)
        
        notificationManager.cancelNotification(PathXNotificationManager.NOTIFICATION_ID_TASK_DUE + taskId)
        notificationManager.cancelNotification(PathXNotificationManager.NOTIFICATION_ID_TASK_OVERDUE + taskId)
    }
    
    fun cancelProjectNotifications(projectId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PathXNotificationManager.NOTIFICATION_ID_PROJECT_DUE + projectId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        notificationManager.cancelNotification(PathXNotificationManager.NOTIFICATION_ID_PROJECT_DUE + projectId)
    }
}
