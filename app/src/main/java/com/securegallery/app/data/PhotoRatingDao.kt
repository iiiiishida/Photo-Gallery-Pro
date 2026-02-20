package com.securegallery.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoRatingDao {
    @Query("SELECT * FROM photo_ratings WHERE uri IN (:uris)")
    suspend fun getByUris(uris: List<String>): List<PhotoRating>

    @Query("SELECT rating FROM photo_ratings WHERE uri = :uri")
    suspend fun get(uri: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(rating: PhotoRating)
}
