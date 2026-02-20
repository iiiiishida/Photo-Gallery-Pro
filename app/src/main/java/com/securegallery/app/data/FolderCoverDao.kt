package com.securegallery.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FolderCoverDao {
    @Query("SELECT * FROM folder_covers WHERE folderPath = :path")
    suspend fun get(path: String): FolderCover?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cover: FolderCover)

    @Query("SELECT * FROM folder_covers")
    suspend fun getAll(): List<FolderCover>
}
