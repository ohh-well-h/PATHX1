package com.example.pathx01.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pathx01.data.DataManager
import com.example.pathx01.data.model.Task

class TaskActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "MARK_COMPLETE" -> {
                val taskId = intent.getIntExtra("task_id", -1)
                if (taskId != -1) {
                    // Mark task as complete
                    val task = DataManager.getTasks().find { it.id == taskId }
                    task?.let {
                        val updatedTask = it.copy(isCompleted = true)
                        DataManager.updateTask(updatedTask)
                        
                        // Cancel the notification
                        val notificationManager = PathXNotificationManager(context)
                        notificationManager.cancelNotification(
                            PathXNotificationManager.NOTIFICATION_ID_TASK_DUE + taskId
                        )
                        notificationManager.cancelNotification(
                            PathXNotificationManager.NOTIFICATION_ID_TASK_OVERDUE + taskId
                        )
                    }
                }
            }
        }
    }
}
