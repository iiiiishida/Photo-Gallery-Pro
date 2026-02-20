package com.securegallery.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        HiddenItem::class,
        TrashItem::class,
        FolderCover::class,
        LuxPreset::class,
        AppSettings::class,
        PhotoRating::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hiddenDao(): HiddenDao
    abstract fun trashDao(): TrashDao
    abstract fun folderCoverDao(): FolderCoverDao
    abstract fun luxPresetDao(): LuxPresetDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun photoRatingDao(): PhotoRatingDao
}
