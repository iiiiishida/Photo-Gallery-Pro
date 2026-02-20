package com.securegallery.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lux_presets")
data class LuxPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val params: LuxParams,
    val createdAt: Long = System.currentTimeMillis()
)
