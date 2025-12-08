package com.example.pathx01.utils

import com.example.pathx01.R

data class Mood(
    val name: String,
    val iconRes: Int,
    val color: Long
)

object MoodUtils {
    val moods = listOf(
        Mood("Happy", R.drawable.ic_mood_happy, 0xFF4CAF50), // Green
        Mood("Excited", R.drawable.ic_mood_excited, 0xFFFF9800), // Orange
        Mood("Neutral", R.drawable.ic_mood_neutral, 0xFF9E9E9E), // Gray
        Mood("Sad", R.drawable.ic_mood_sad, 0xFF2196F3), // Blue
        Mood("Anxious", R.drawable.ic_mood_anxious, 0xFFF44336) // Red
    )
    
    fun getMoodByName(name: String): Mood? {
        return moods.find { it.name.equals(name, ignoreCase = true) }
    }
    
    fun getDefaultMood(): Mood {
        return moods[2] // Neutral
    }
}


