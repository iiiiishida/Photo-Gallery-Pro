package com.securegallery.app.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val path: String?,
    val displayName: String,
    val sizeBytes: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val dateTaken: Long,
    val mimeType: String?,
    val width: Int,
    val height: Int,
    val bucketId: String?,
    val bucketName: String?,
    val rating: Int = 0
)

enum class SortType {
    TYPE, SIZE, DATE_TAKEN, DATE_ADDED, DATE_MODIFIED, RATING
}

class GalleryRepository(private val context: Context, private val db: AppDatabase) {

    private val hiddenDao = db.hiddenDao()
    private val trashDao = db.trashDao()
    private val folderCoverDao = db.folderCoverDao()
    private val settingsDao = db.appSettingsDao()
    private val ratingDao = db.photoRatingDao()

    suspend fun getTrashDays(): Int =
        settingsDao.get(KEY_TRASH_DAYS)?.toIntOrNull() ?: 30

    suspend fun setTrashDays(days: Int) {
        settingsDao.set(AppSettings(KEY_TRASH_DAYS, days.toString()))
    }

    suspend fun getMinSizeKb(): Long =
        settingsDao.get(KEY_HIDE_BELOW_KB)?.toLongOrNull() ?: 0L

    suspend fun setMinSizeKb(kb: Long) {
        settingsDao.set(AppSettings(KEY_HIDE_BELOW_KB, kb.toString()))
    }

    fun loadAllPhotos(sort: SortType, hideBelowKb: Long): Flow<List<MediaItem>> = flow {
        val hiddenUris = hiddenDao.getAllUris().toSet()
        val trashUris = trashDao.getAll().map { it.uri }.toSet()
        val items = withContext(Dispatchers.IO) {
            val list = queryMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, sort = sort)
                .filter { it.uri.toString() !in hiddenUris && it.uri.toString() !in trashUris }
                .filter { hideBelowKb <= 0 || it.sizeBytes >= hideBelowKb * 1024 }
            applyRatingsAndSort(list, sort)
        }
        emit(items)
    }

    fun loadCameraPhotos(sort: SortType, hideBelowKb: Long): Flow<List<MediaItem>> = flow {
        val hiddenUris = hiddenDao.getAllUris().toSet()
        val trashUris = trashDao.getAll().map { it.uri }.toSet()
        val items = withContext(Dispatchers.IO) {
            val list = queryMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, sort = sort)
                .filter { item ->
                    item.path?.contains("DCIM/Camera") == true ||
                        item.bucketName.equals("Camera", ignoreCase = true)
                }
                .filter { it.uri.toString() !in hiddenUris && it.uri.toString() !in trashUris }
                .filter { hideBelowKb <= 0 || it.sizeBytes >= hideBelowKb * 1024 }
            applyRatingsAndSort(list, sort)
        }
        emit(items)
    }

    fun loadByFolders(sort: SortType, hideBelowKb: Long): Flow<Map<String, List<MediaItem>>> = flow {
        val hiddenUris = hiddenDao.getAllUris().toSet()
        val trashUris = trashDao.getAll().map { it.uri }.toSet()
        val all = withContext(Dispatchers.IO) {
            val list = queryMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, sort = sort)
                .filter { it.uri.toString() !in hiddenUris && it.uri.toString() !in trashUris }
                .filter { hideBelowKb <= 0 || it.sizeBytes >= hideBelowKb * 1024 }
            applyRatingsAndSort(list, sort)
        }
        val byFolder = all.groupBy { item ->
            item.path?.substringBeforeLast("/") ?: item.bucketName ?: "Unknown"
        }
        emit(byFolder)
    }

    fun loadTrash(): Flow<List<TrashItem>> = flow {
        emit(trashDao.getAll())
    }

    suspend fun isHidden(uri: String): Boolean = hiddenDao.isHidden(uri) > 0

    suspend fun toggleHidden(uri: String): Boolean = hiddenDao.toggle(uri)

    suspend fun moveToTrash(uri: String, originalPath: String, displayName: String) {
        trashDao.insert(TrashItem(uri = uri, originalPath = originalPath, displayName = displayName))
    }

    suspend fun restoreFromTrash(uri: String) {
        trashDao.delete(uri)
    }

    suspend fun deleteFromTrash(uri: String) {
        trashDao.delete(uri)
    }

    suspend fun cleanupTrashOlderThan(days: Int) {
        val before = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000
        trashDao.deleteOlderThan(before)
    }

    suspend fun getFolderCover(path: String): FolderCover? = folderCoverDao.get(path)

    suspend fun setFolderCover(path: String, coverUri: String?, useBlur: Boolean) {
        folderCoverDao.upsert(FolderCover(folderPath = path, coverUri = coverUri, useBlur = useBlur))
    }

    suspend fun getAllFolderCoversMap(): Map<String, String?> =
        folderCoverDao.getAll().associate { it.folderPath to it.coverUri }

    suspend fun getRating(uri: String): Int = ratingDao.get(uri) ?: 0

    suspend fun setRating(uri: String, rating: Int) {
        if (rating !in 0..5) return
        ratingDao.set(PhotoRating(uri = uri, rating = rating))
    }

    private suspend fun applyRatingsAndSort(items: List<MediaItem>, sort: SortType): List<MediaItem> {
        if (items.isEmpty()) return items
        val uris = items.map { it.uri.toString() }
        val ratings = ratingDao.getByUris(uris).associate { it.uri to it.rating }
        val merged = items.map { it.copy(rating = ratings[it.uri.toString()] ?: 0) }
        return when (sort) {
            SortType.RATING -> merged.sortedWith(compareByDescending<MediaItem> { it.rating }.thenByDescending { it.dateAdded })
            else -> merged
        }
    }

    /**
     * 使用 ContentResolver 查詢 External 存儲中的圖片，僅以 MIME_TYPE LIKE 'image/%' 為條件，
     * 確保在「全部允許」或「部分選取」權限下都能正確回傳可見項目。呼叫處需在 Dispatchers.IO 執行。
     */
    private fun queryMedia(baseUri: Uri, sort: SortType): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val sortOrder = when (sort) {
            SortType.DATE_TAKEN -> "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            SortType.DATE_ADDED -> "${MediaStore.Images.Media.DATE_ADDED} DESC"
            SortType.DATE_MODIFIED -> "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            SortType.SIZE -> "${MediaStore.Images.Media.SIZE} DESC"
            SortType.TYPE -> "${MediaStore.Images.Media.MIME_TYPE} ASC"
            SortType.RATING -> "${MediaStore.Images.Media.DATE_ADDED} DESC"
        }
        val selection = "${MediaStore.Images.Media.MIME_TYPE} LIKE ?"
        val selectionArgs = arrayOf("image/%")
        context.contentResolver.query(
            baseUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val addedIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val modIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val takenIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val mimeIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val wIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val hIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val list = mutableListOf<MediaItem>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val uri = ContentUris.withAppendedId(baseUri, id)
                list.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        path = if (dataIdx >= 0) cursor.getString(dataIdx) else null,
                        displayName = cursor.getString(nameIdx) ?: "",
                        sizeBytes = cursor.getLong(sizeIdx),
                        dateAdded = cursor.getLong(addedIdx) * 1000,
                        dateModified = cursor.getLong(modIdx) * 1000,
                        dateTaken = cursor.getLong(takenIdx).let { if (it > 0) it else cursor.getLong(addedIdx) * 1000 },
                        mimeType = cursor.getString(mimeIdx),
                        width = cursor.getInt(wIdx),
                        height = cursor.getInt(hIdx),
                        bucketId = cursor.getString(bucketIdIdx),
                        bucketName = cursor.getString(bucketNameIdx)
                    )
                )
            }
            return list
        }
        return emptyList()
    }

    companion object {
        private const val KEY_TRASH_DAYS = "trash_days"
        private const val KEY_HIDE_BELOW_KB = "hide_below_kb"
    }
}
