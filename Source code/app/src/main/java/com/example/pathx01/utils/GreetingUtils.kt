package com.example.pathx01.utils

import com.example.pathx01.R
import java.time.LocalTime

object GreetingUtils {
    private val royalTitles = listOf("Your Honor", "My Queen", "Your Highness", "Princess")
    
    // Enhanced greetings for normal users
    private val morningGreetings = listOf(
        "Good Morning", "Rise and Shine", "Morning Sunshine", "Hello there", "Top of the morning"
    )
    
    private val afternoonGreetings = listOf(
        "Good Afternoon", "Hope your day is going well", "Afternoon", "Hello", "How's your day"
    )
    
    private val eveningGreetings = listOf(
        "Good Evening", "Evening", "Hope you had a great day", "Hello there", "How was your day"
    )
    
    private val nightGreetings = listOf(
        "Good Night", "Working late", "Night owl", "Hello night warrior", "Burning the midnight oil"
    )
    
    fun getTimeBasedGreeting(userName: String): String {
        val currentTime = LocalTime.now()
        val hour = currentTime.hour
        
        return if (userName.isNotBlank()) {
            when {
                userName.equals("Hebron", ignoreCase = true) -> {
                    val baseGreeting = when (hour) {
                        in 5..11 -> "Good Morning"
                        in 12..17 -> "Good Afternoon"
                        in 18..23 -> "Good Evening"
                        else -> "Good Night"
                    }
                    val randomTitle = royalTitles.random()
                    "$baseGreeting, $randomTitle"
                }
                userName.equals("Calvin", ignoreCase = true) -> {
                    val baseGreeting = when (hour) {
                        in 5..11 -> "Good Morning"
                        in 12..17 -> "Good Afternoon"
                        in 18..23 -> "Good Evening"
                        else -> "Good Night"
                    }
                    "$baseGreeting, Calvin"
                }
                else -> {
                    // Enhanced greetings for normal users
                    val greetingList = when (hour) {
                        in 5..11 -> morningGreetings
                        in 12..17 -> afternoonGreetings
                        in 18..23 -> eveningGreetings
                        else -> nightGreetings
                    }
                    val randomGreeting = greetingList.random()
                    "$randomGreeting, $userName"
                }
            }
        } else {
            val greeting = when (hour) {
                in 5..11 -> morningGreetings.random()
                in 12..17 -> afternoonGreetings.random()
                in 18..23 -> eveningGreetings.random()
                else -> nightGreetings.random()
            }
            greeting
        }
    }
    
    fun getGreetingIconRes(): Int {
        val currentTime = LocalTime.now()
        val hour = currentTime.hour
        
        return when (hour) {
            in 5..11 -> R.drawable.ic_sun // Morning sun
            in 12..17 -> R.drawable.ic_sun // Afternoon sun
            in 18..23 -> R.drawable.ic_moon // Evening moon
            else -> R.drawable.ic_star // Night stars
        }
    }
}
