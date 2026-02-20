package com.securegallery.app.data

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadImageMetadata(context: Context, uri: Uri): Map<String, String> = withContext(Dispatchers.IO) {
    val result = mutableMapOf<String, String>()
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            exif.getAttribute(ExifInterface.TAG_MAKE)?.let { result["Camera make"] = it }
            exif.getAttribute(ExifInterface.TAG_MODEL)?.let { result["Camera model"] = it }
            exif.getAttribute(ExifInterface.TAG_LENS_MODEL)?.let { result["Lens"] = it }
            exif.getAttribute(ExifInterface.TAG_DATETIME)?.let { result["EXIF date"] = it }
            exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { result["Aperture"] = it }
            exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)?.let { result["ISO"] = it }
            exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { result["Shutter"] = it }
            exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { result["Focal length"] = it }
            exif.getAttribute(ExifInterface.TAG_ORIENTATION)?.let { result["Orientation"] = it }
        }
    } catch (_: Exception) { }
    result
}
