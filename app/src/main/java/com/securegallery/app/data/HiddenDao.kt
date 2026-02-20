package com.securegallery.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface HiddenDao {
    @Query("SELECT uri FROM hidden_items")
    suspend fun getAllUris(): List<String>

    @Query("SELECT COUNT(*) FROM hidden_items WHERE uri = :uri")
    suspend fun isHidden(uri: String): Int

    @Insert
    suspend fun insert(item: HiddenItem)

    @Query("DELETE FROM hidden_items WHERE uri = :uri")
    suspend fun remove(uri: String)

    @Transaction
    suspend fun toggle(uri: String): Boolean {
        return if (isHidden(uri) > 0) {
            remove(uri)
            false
        } else {
            insert(HiddenItem(uri = uri))
            true
        }
    }
}
