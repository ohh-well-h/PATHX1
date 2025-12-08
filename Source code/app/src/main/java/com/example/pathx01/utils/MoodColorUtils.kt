package com.example.pathx01.utils

import androidx.compose.ui.graphics.Color
import com.example.pathx01.data.model.Mood

object MoodColorUtils {
    fun getMoodColor(mood: Mood): Color {
        return when (mood) {
            Mood.HAPPY -> Color(0xFF4CAF50) // Green
            Mood.TIRED -> Color(0xFF9E9E9E) // Gray
            Mood.MOTIVATED -> Color(0xFF2196F3) // Blue
            Mood.STRESSED -> Color(0xFFF44336) // Red
            Mood.CONTENT -> Color(0xFF8BC34A) // Light Green
            Mood.EXCITED -> Color(0xFFFF9800) // Orange
            Mood.ANXIOUS -> Color(0xFF9C27B0) // Purple
            Mood.PROUD -> Color(0xFF3F51B5) // Indigo
            Mood.FRUSTRATED -> Color(0xFFE91E63) // Pink
            Mood.GRATEFUL -> Color(0xFFFFC107) // Amber
        }
    }
    
    fun getMoodBackgroundColor(mood: Mood): Color {
        return when (mood) {
            Mood.HAPPY -> Color(0xFFE8F5E8) // Light Green
            Mood.TIRED -> Color(0xFFF5F5F5) // Light Gray
            Mood.MOTIVATED -> Color(0xFFE3F2FD) // Light Blue
            Mood.STRESSED -> Color(0xFFFFEBEE) // Light Red
            Mood.CONTENT -> Color(0xFFF1F8E9) // Light Green
            Mood.EXCITED -> Color(0xFFFFF3E0) // Light Orange
            Mood.ANXIOUS -> Color(0xFFF3E5F5) // Light Purple
            Mood.PROUD -> Color(0xFFE8EAF6) // Light Indigo
            Mood.FRUSTRATED -> Color(0xFFFCE4EC) // Light Pink
            Mood.GRATEFUL -> Color(0xFFFFF8E1) // Light Amber
        }
    }
    
    fun getMoodIcon(mood: Mood): String {
        return when (mood) {
            Mood.HAPPY -> "😊"
            Mood.TIRED -> "😴"
            Mood.MOTIVATED -> "💪"
            Mood.STRESSED -> "😰"
            Mood.CONTENT -> "😌"
            Mood.EXCITED -> "🤩"
            Mood.ANXIOUS -> "😟"
            Mood.PROUD -> "😎"
            Mood.FRUSTRATED -> "😤"
            Mood.GRATEFUL -> "🙏"
        }
    }
}
