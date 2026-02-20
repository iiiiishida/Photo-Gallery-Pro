package com.securegallery.app.ui.viewer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.securegallery.app.data.MediaItem

@Composable
fun StarRating(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { star ->
            IconButton(onClick = {
                // 點同一顆 = 取消該星；點新星 = 設為該星等
                onRatingChange(if (rating == star) 0 else star)
            }) {
                Icon(
                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$star star",
                    tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    item: MediaItem,
    onRatingChange: ((Int) -> Unit)?,
    onBack: () -> Unit,
    onEdit: (MediaItem) -> Unit,
    onShare: (MediaItem) -> Unit,
    onShowMetadata: (MediaItem) -> Unit,
    onMoveCopy: (MediaItem) -> Unit,
    onSetAsFolderCover: (MediaItem) -> Unit,
    onSetAsWallpaper: (MediaItem) -> Unit,
    onRename: (MediaItem) -> Unit,
    onHide: (MediaItem) -> Unit,
    onDelete: (MediaItem) -> Unit
) {
    val barColor = Color.Black.copy(alpha = 0.6f)
    var showRateDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Top Bar: Back + Info/Edit/Rate
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = barColor),
            title = { Text(item.displayName, maxLines = 1, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
            },
            actions = {
                TextButton(onClick = { onShowMetadata(item) }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Info", color = Color.White)
                }
                TextButton(onClick = { onEdit(item) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Edit", color = Color.White)
                }
                TextButton(
                    onClick = { if (onRatingChange != null) showRateDialog = true },
                    enabled = onRatingChange != null
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Rate", tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Rate", color = Color.White)
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Set as folder cover") },
                        onClick = {
                            showMenu = false
                            onSetAsFolderCover(item)
                        },
                        leadingIcon = { Icon(Icons.Default.Photo, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Set as wallpaper") },
                        onClick = {
                            showMenu = false
                            onSetAsWallpaper(item)
                        },
                        leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = null) }
                    )
                }
            }
        )

        // Bottom Bar: Move/Share/Delete
        BottomAppBar(
            containerColor = barColor,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onMoveCopy(item) }) {
                    Icon(Icons.Default.DriveFileMove, contentDescription = "Move", tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Move", color = Color.White)
                }
                TextButton(onClick = { onShare(item) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Share", color = Color.White)
                }
                TextButton(onClick = { onDelete(item) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Rate dialog
        if (showRateDialog && onRatingChange != null) {
            AlertDialog(
                onDismissRequest = { showRateDialog = false },
                containerColor = barColor,
                title = { Text("Rate", color = Color.White) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StarRating(rating = item.rating, onRatingChange = onRatingChange)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap a star to set or clear rating.", color = Color.White)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRateDialog = false }) { Text("Done", color = Color.White) }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataScreen(
    item: MediaItem,
    extraInfo: Map<String, String>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            listOf(
                "File name" to item.displayName,
                "Rating" to (if (item.rating > 0) "${item.rating} star(s)" else "Not rated"),
                "Path" to (item.path ?: "-"),
                "Size" to "${item.sizeBytes / 1024} KB",
                "Dimensions" to "${item.width} × ${item.height}",
                "MIME" to (item.mimeType ?: "-"),
                "Date taken" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.dateTaken)),
                "Date added" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.dateAdded)),
                "Date modified" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.dateModified)),
                "Folder" to (item.bucketName ?: "-")
            ).plus(extraInfo.entries.map { it.key to it.value }).forEach { (label, value) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(value, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
                Divider()
            }
        }
    }
}
