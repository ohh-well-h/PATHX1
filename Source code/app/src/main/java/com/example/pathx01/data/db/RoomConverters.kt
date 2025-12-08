package com.example.pathx01.data.db

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RoomConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.format(formatter)

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, formatter) }
}


