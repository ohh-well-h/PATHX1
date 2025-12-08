package com.example.pathx01.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.pathx01.data.model.UserPreferences

class UserPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    
    fun saveUserPreferences(preferences: UserPreferences) {
        sharedPreferences.edit().apply {
            putString("user_name", preferences.userName)
            putBoolean("is_first_launch", preferences.isFirstLaunch)
            apply()
        }
    }
    
    fun getUserPreferences(): UserPreferences {
        return UserPreferences(
            userName = sharedPreferences.getString("user_name", "") ?: "",
            isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true)
        )
    }
    
    fun updateUserName(name: String) {
        sharedPreferences.edit().apply {
            putString("user_name", name)
            putBoolean("is_first_launch", false)
            apply()
        }
    }
    
    fun getOriginalUserName(): String {
        return sharedPreferences.getString("original_user_name", "") ?: ""
    }
    
    fun setOriginalUserName(name: String) {
        // Only set if not already set (first time only)
        if (getOriginalUserName().isEmpty()) {
            sharedPreferences.edit().apply {
                putString("original_user_name", name)
                apply()
            }
        }
    }
    
    fun isSpecialUser(): Boolean {
        val originalName = getOriginalUserName()
        return originalName.equals("Hebron", ignoreCase = true) || 
               originalName.equals("Calvin", ignoreCase = true)
    }
    
    fun getSpecialUserType(): String? {
        val originalName = getOriginalUserName()
        return when {
            originalName.equals("Hebron", ignoreCase = true) -> "Hebron"
            originalName.equals("Calvin", ignoreCase = true) -> "Calvin"
            else -> null
        }
    }
    
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("is_first_launch", true)
    }
    
    fun getUserName(): String {
        return sharedPreferences.getString("user_name", "") ?: ""
    }
    
    fun hasSeenExampleDataDialog(): Boolean {
        return sharedPreferences.getBoolean("has_seen_example_data_dialog", false)
    }
    
    fun setExampleDataDialogSeen() {
        sharedPreferences.edit().apply {
            putBoolean("has_seen_example_data_dialog", true)
            apply()
        }
    }
}

