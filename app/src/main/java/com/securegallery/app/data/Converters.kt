package com.securegallery.app.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun fromLuxParams(value: LuxParams?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toLuxParams(value: String?): LuxParams? =
        value?.let { gson.fromJson(it, LuxParams::class.java) }
}
