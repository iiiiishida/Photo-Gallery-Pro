package com.securegallery.app.ui

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.securegallery.app.SecureGalleryApp
import com.securegallery.app.data.GalleryRepository
import com.securegallery.app.data.MediaItem
import com.securegallery.app.data.SecurityManager
import com.securegallery.app.data.SortType
import com.securegallery.app.data.TrashItem
import com.securegallery.app.data.loadImageMetadata
import com.securegallery.app.ui.gallery.FolderGrid
import com.securegallery.app.ui.gallery.PhotoGrid
import com.securegallery.app.ui.gallery.SortMenu
import com.securegallery.app.ui.gallery.TrashGrid
import com.securegallery.app.ui.edit.EditImageScreen
import com.securegallery.app.ui.settings.SettingsScreen
import com.securegallery.app.ui.viewer.ImageViewerScreen
import com.securegallery.app.ui.viewer.MetadataScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class GalleryTab { CAMERA, ALL, CATEGORIES, TRASH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGallery(
    app: SecureGalleryApp,
    security: SecurityManager,
    onLock: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { GalleryRepository(context, app.database) }
    val scope = rememberCoroutineScope()
    val imageLoader = remember { ImageLoader(context) }

    suspend fun loadBitmapForWallpaper(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val req = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(req)
            val drawable = (result as? SuccessResult)?.drawable ?: return@runCatching null
            drawable.toBitmap()
        }.getOrNull()
    }

    var currentTab by remember { mutableStateOf(GalleryTab.ALL) }
    var sortType by remember { mutableStateOf(SortType.DATE_ADDED) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
    var showMetadata by remember { mutableStateOf<MediaItem?>(null) }
    var showEdit by remember { mutableStateOf<MediaItem?>(null) }
    var showSharePin by remember { mutableStateOf<MediaItem?>(null) }
    var showMoveCopy by remember { mutableStateOf<MediaItem?>(null) }
    var showRename by remember { mutableStateOf<MediaItem?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var folderToShow by remember { mutableStateOf<Pair<String, List<MediaItem>>?>(null) }
    var trashItems by remember { mutableStateOf<List<TrashItem>>(emptyList()) }
    var allPhotos by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var cameraPhotos by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var folders by remember { mutableStateOf<Map<String, List<MediaItem>>>(emptyMap()) }
    var folderCovers by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    var hideBelowKb by remember { mutableStateOf(0L) }

    LaunchedEffect(currentTab, sortType) {
        hideBelowKb = repo.getMinSizeKb()
        when (currentTab) {
            GalleryTab.ALL -> repo.loadAllPhotos(sortType, hideBelowKb).collect { allPhotos = it }
            GalleryTab.CAMERA -> repo.loadCameraPhotos(sortType, hideBelowKb).collect { cameraPhotos = it }
            GalleryTab.CATEGORIES -> repo.loadByFolders(sortType, hideBelowKb).collect {
                folders = it
                folderCovers = repo.getAllFolderCoversMap()
            }
            GalleryTab.TRASH -> repo.loadTrash().collect { trashItems = it }
        }
    }

    val listForTab = when (currentTab) {
        GalleryTab.ALL -> allPhotos
        GalleryTab.CAMERA -> cameraPhotos
        GalleryTab.CATEGORIES -> folderToShow?.second ?: emptyList()
        GalleryTab.TRASH -> emptyList()
    }

    // Viewer
    selectedItem?.let { item ->
        ImageViewerScreen(
            item = item,
            onRatingChange = { r ->
                scope.launch {
                    repo.setRating(item.uri.toString(), r)
                    selectedItem = item.copy(rating = r)
                }
            },
            onBack = { selectedItem = null },
            onEdit = { mediaItem -> showEdit = mediaItem; selectedItem = null },
            onShare = { mediaItem -> showSharePin = mediaItem; selectedItem = null },
            onShowMetadata = { mediaItem -> showMetadata = mediaItem; selectedItem = null },
            onMoveCopy = { mediaItem -> showMoveCopy = mediaItem; selectedItem = null },
            onSetAsFolderCover = { media ->
                scope.launch {
                    val folderKey =
                        media.bucketId
                            ?: media.bucketName
                            ?: media.path?.substringBeforeLast("/")
                            ?: "Unknown"
                    repo.setFolderCover(folderKey, media.uri.toString(), useBlur = false)
                    folderCovers = repo.getAllFolderCoversMap()
                }
            },
            onSetAsWallpaper = { media ->
                val ctx = context
                scope.launch {
                    loadBitmapForWallpaper(media.uri)
                    val wm = android.app.WallpaperManager.getInstance(ctx)
                    val i = try {
                        wm.getCropAndSetWallpaperIntent(media.uri)
                    } catch (_: Throwable) {
                        android.content.Intent(android.app.WallpaperManager.ACTION_SET_WALLPAPER).apply {
                            type = "image/*"
                            putExtra(android.content.Intent.EXTRA_STREAM, media.uri)
                        }
                    }
                    ctx.startActivity(i.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION))
                }
            },
            onRename = { mediaItem -> showRename = mediaItem; selectedItem = null },
            onHide = {
                scope.launch { repo.toggleHidden(it.uri.toString()) }
                selectedItem = null
            },
            onDelete = {
                scope.launch {
                    repo.moveToTrash(it.uri.toString(), it.path ?: "", it.displayName)
                    selectedItem = null
                }
            }
        )
        return
    }

    // Metadata
    showMetadata?.let { item ->
        var metadataExtra by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
        LaunchedEffect(item.uri) {
            metadataExtra = loadImageMetadata(context, item.uri)
        }
        MetadataScreen(
            item = item,
            extraInfo = metadataExtra,
            onBack = { showMetadata = null }
        )
        return
    }

    // Share PIN dialog
    showSharePin?.let { item ->
        var pin by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showSharePin = null },
            title = { Text("Enter PIN to share") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        if (security.verifyPin(pin)) {
                            context.startActivity(Intent(Intent.ACTION_SEND).apply {
                                type = "image/*"
                                putExtra(Intent.EXTRA_STREAM, item.uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            })
                            showSharePin = null
                        } else err = "Wrong PIN"
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showSharePin = null }) { Text("Cancel") } }
        )
    }

    // Edit (simplified - just open edit screen)
    showEdit?.let { item ->
        EditImageScreen(
            currentParams = com.securegallery.app.data.LuxParams(),
            luxPresetNames = emptyList(),
            onSave = { showEdit = null },
            onSaveAsLux = { _, _ -> },
            onApplyPreset = { },
            onBack = { showEdit = null }
        )
        return
    }

    // Move/Copy / Rename - simple placeholder
    if (showMoveCopy != null || showRename != null) {
        AlertDialog(
            onDismissRequest = { showMoveCopy = null; showRename = null },
            title = { Text(if (showRename != null) "Rename" else "Move / Copy") },
            text = { Text("Choose target folder in Settings, or use in a future version.") },
            confirmButton = { TextButton(onClick = { showMoveCopy = null; showRename = null }) { Text("OK") } }
        )
    }

    // Settings
    if (showSettings) {
        var settingsTrashDays by remember { mutableStateOf(30) }
        var settingsHideKb by remember { mutableStateOf(0L) }
        LaunchedEffect(showSettings) {
            if (showSettings) {
                settingsTrashDays = repo.getTrashDays()
                settingsHideKb = repo.getMinSizeKb()
            }
        }
        SettingsScreen(
            trashDays = settingsTrashDays,
            hideBelowKb = settingsHideKb,
            onTrashDaysChange = {
                scope.launch { repo.setTrashDays(it) }
                settingsTrashDays = it
            },
            onHideBelowKbChange = {
                scope.launch { repo.setMinSizeKb(it) }
                settingsHideKb = it
            },
            onBack = { showSettings = false }
        )
        return
    }

    // Main content: tabs + sort
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(Modifier.fillMaxWidth()) {
                        if (folderToShow != null) {
                            IconButton(onClick = { folderToShow = null }) {
                                Icon(Icons.Default.ArrowBack, "Back")
                            }
                        }
                        Text(
                            when (currentTab) {
                                GalleryTab.CAMERA -> "Camera"
                                GalleryTab.ALL -> "All Photos"
                                GalleryTab.CATEGORIES -> (folderToShow?.first ?: "Categories")
                                GalleryTab.TRASH -> "Trash"
                            }
                        )
                    }
                },
                actions = {
                    if (currentTab != GalleryTab.TRASH) {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "Sort")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortMenu(
                                currentSort = sortType,
                                onDismiss = { showSortMenu = false },
                                onSortSelected = { sortType = it }
                            )
                        }
                    }
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Settings, "Settings") }
                    IconButton(onClick = onLock) { Icon(Icons.Default.Security, "Lock") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == GalleryTab.CAMERA,
                    onClick = { currentTab = GalleryTab.CAMERA; folderToShow = null },
                    icon = { Icon(Icons.Default.CameraAlt, "Camera") },
                    label = { Text("Camera") }
                )
                NavigationBarItem(
                    selected = currentTab == GalleryTab.ALL,
                    onClick = { currentTab = GalleryTab.ALL; folderToShow = null },
                    icon = { Icon(Icons.Default.PhotoLibrary, "All Photos") },
                    label = { Text("All Photos") }
                )
                NavigationBarItem(
                    selected = currentTab == GalleryTab.CATEGORIES && folderToShow == null,
                    onClick = { currentTab = GalleryTab.CATEGORIES; folderToShow = null },
                    icon = { Icon(Icons.Default.Folder, "Categories") },
                    label = { Text("Categories") }
                )
                NavigationBarItem(
                    selected = currentTab == GalleryTab.TRASH,
                    onClick = { currentTab = GalleryTab.TRASH },
                    icon = { Icon(Icons.Default.Delete, "Trash") },
                    label = { Text("Trash") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (currentTab) {
                GalleryTab.CAMERA, GalleryTab.ALL, GalleryTab.CATEGORIES -> {
                    if (currentTab == GalleryTab.CATEGORIES && folderToShow == null) {
                        FolderGrid(
                            folders = folders,
                            coverUris = folderCovers,
                            onFolderClick = { name, list -> folderToShow = name to list },
                            onPhotoClick = { selectedItem = it }
                        )
                    } else {
                        PhotoGrid(
                            items = listForTab,
                            onPhotoClick = { selectedItem = it },
                            onPhotoLongClick = { selectedItem = it }
                        )
                    }
                }
                GalleryTab.TRASH -> TrashGrid(
                    items = trashItems,
                    onItemClick = { },
                    onRestore = { scope.launch { repo.restoreFromTrash(it.uri) } },
                    onDelete = { scope.launch { repo.deleteFromTrash(it.uri) } }
                )
            }
        }
    }
}
