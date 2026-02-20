package com.securegallery.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_items")
data class HiddenItem(
    @PrimaryKey val uri: String,
    val addedAt: Long = System.currentTimeMillis()
)
