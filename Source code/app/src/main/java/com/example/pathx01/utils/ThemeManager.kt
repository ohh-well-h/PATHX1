package com.example.pathx01.utils

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
    
    fun getThemePreference(): String {
        return prefs.getString("theme_preference", THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    fun setThemePreference(theme: String) {
        prefs.edit().putString("theme_preference", theme).apply()
    }
    
    fun isDarkTheme(): Boolean {
        return when (getThemePreference()) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }
    
    fun getThemeIcon(): String {
        return when (getThemePreference()) {
            THEME_DARK -> "sun" // Show sun icon when in dark mode (to switch to light)
            THEME_LIGHT -> "moon" // Show moon icon when in light mode (to switch to dark)
            THEME_SYSTEM -> {
                if (isDarkTheme()) "sun" else "moon"
            }
            else -> "moon"
        }
    }
    
    fun toggleTheme() {
        val currentTheme = getThemePreference()
        when (currentTheme) {
            THEME_SYSTEM -> {
                // If system, toggle to opposite of current system theme
                if (isDarkTheme()) {
                    setThemePreference(THEME_LIGHT)
                } else {
                    setThemePreference(THEME_DARK)
                }
            }
            THEME_LIGHT -> setThemePreference(THEME_DARK)
            THEME_DARK -> setThemePreference(THEME_LIGHT)
        }
    }
}
