package com.example.pathx01.data.converters

import androidx.room.TypeConverter
import com.example.pathx01.data.model.Priority

object PriorityConverters {
    @TypeConverter
    @JvmStatic
    fun fromPriority(value: Priority?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun toPriority(value: String?): Priority? = value?.let { runCatching { Priority.valueOf(it) }.getOrNull() }
}






