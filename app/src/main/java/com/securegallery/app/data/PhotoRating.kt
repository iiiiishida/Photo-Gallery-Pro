package com.securegallery.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 相片評等 (0 = 未評等, 1–5 = 星等)，與 Windows 評等相容 */
@Entity(tableName = "photo_ratings")
data class PhotoRating(
    @PrimaryKey val uri: String,
    val rating: Int,
    val updatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(rating in 0..5) { "rating must be 0-5" }
    }
}
