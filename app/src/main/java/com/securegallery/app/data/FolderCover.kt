package com.securegallery.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_covers")
data class FolderCover(
    @PrimaryKey val folderPath: String,
    val coverUri: String? = null,
    val useBlur: Boolean = false
)
