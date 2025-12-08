package com.example.pathx01.data

import com.example.pathx01.data.model.Task
import com.example.pathx01.data.model.Priority
import java.time.LocalDateTime

object SampleData {
    
    fun getSampleTasks(): List<Task> {
        val now = LocalDateTime.now()
        return listOf(
            Task(
                title = "Complete Math Assignment",
                description = "Finish calculus problems 1-20",
                category = "Academic",
                priority = Priority.HIGH,
                dueDate = now.plusDays(2).toString()
            ),
            Task(
                title = "SAT Practice Test",
                description = "Take full-length practice test",
                category = "Test Prep",
                priority = Priority.MEDIUM,
                dueDate = now.plusDays(5).toString()
            ),
            Task(
                title = "University Application Deadline",
                description = "Submit applications to top 3 universities",
                category = "College Apps",
                priority = Priority.URGENT,
                dueDate = now.plusDays(10).toString()
            ),
            Task(
                title = "Physics Lab Report",
                description = "Write lab report for optics experiment",
                category = "Academic",
                priority = Priority.MEDIUM,
                dueDate = now.plusDays(3).toString()
            ),
            Task(
                title = "College Essay Draft",
                description = "Complete first draft of personal statement",
                category = "College Apps",
                priority = Priority.HIGH,
                dueDate = now.plusDays(7).toString()
            ),
            Task(
                title = "Study for Chemistry Exam",
                description = "Review chapters 5-8",
                category = "Academic",
                priority = Priority.HIGH,
                isCompleted = true,
                dueDate = now.minusDays(1).toString()
            ),
            Task(
                title = "Gym Workout",
                description = "Upper body strength training",
                category = "Personal",
                priority = Priority.LOW,
                dueDate = now.plusDays(1).toString()
            ),
            Task(
                title = "Read Science Article",
                description = "Read latest research on climate change",
                category = "Reading",
                priority = Priority.LOW,
                isCompleted = true,
                dueDate = now.minusDays(2).toString()
            )
        )
    }
    
    fun getDefaultCategories(): List<String> {
        return listOf(
            "Academic",
            "Test Prep", 
            "College Apps",
            "Personal",
            "Reading",
            "Projects",
            "Health",
            "Finance"
        )
    }
}



