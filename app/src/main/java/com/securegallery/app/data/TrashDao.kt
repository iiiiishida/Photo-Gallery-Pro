package com.securegallery.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrashDao {
    @Query("SELECT * FROM trash_items ORDER BY deletedAt DESC")
    suspend fun getAll(): List<TrashItem>

    @Insert
    suspend fun insert(item: TrashItem)

    @Query("DELETE FROM trash_items WHERE uri = :uri")
    suspend fun delete(uri: String)

    @Query("DELETE FROM trash_items WHERE deletedAt < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT * FROM trash_items WHERE uri = :uri")
    suspend fun getByUri(uri: String): TrashItem?
}
