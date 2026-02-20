package com.securegallery.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LuxPresetDao {
    @Query("SELECT * FROM lux_presets ORDER BY createdAt DESC")
    suspend fun getAll(): List<LuxPreset>

    @Insert
    suspend fun insert(preset: LuxPreset): Long

    @Query("DELETE FROM lux_presets WHERE id = :id")
    suspend fun delete(id: Long)
}
