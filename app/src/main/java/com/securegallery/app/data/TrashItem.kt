package com.securegallery.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_items")
data class TrashItem(
    @PrimaryKey val uri: String,
    val originalPath: String,
    val deletedAt: Long = System.currentTimeMillis(),
    val displayName: String = ""
)
