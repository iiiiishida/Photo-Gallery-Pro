package com.securegallery.app.ui.gallery

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.securegallery.app.data.MediaItem
import com.securegallery.app.data.SortType
import com.securegallery.app.data.TrashItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGrid(
    items: List<MediaItem>,
    onPhotoClick: (MediaItem) -> Unit,
    onPhotoLongClick: (MediaItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .combinedClickable(
                        onClick = { onPhotoClick(item) },
                        onLongClick = { onPhotoLongClick(item) }
                    )
            ) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (item.rating > 0) {
                    Row(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        repeat(item.rating) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortMenu(
    currentSort: SortType,
    onDismiss: () -> Unit,
    onSortSelected: (SortType) -> Unit
) {
    val options = listOf(
        SortType.TYPE to "Type",
        SortType.SIZE to "Size",
        SortType.DATE_TAKEN to "Date taken",
        SortType.DATE_ADDED to "Date added",
        SortType.DATE_MODIFIED to "Date modified",
        SortType.RATING to "Rating"
    )
    options.forEach { (sort, label) ->
        DropdownMenuItem(
            text = { Text(label) },
            onClick = {
                onSortSelected(sort)
                onDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashGrid(
    items: List<TrashItem>,
    onItemClick: (TrashItem) -> Unit,
    onRestore: (TrashItem) -> Unit,
    onDelete: (TrashItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp),
                onClick = { onItemClick(item) }
            ) {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(onClick = { onRestore(item) }) {
                            Icon(Icons.Default.Restore, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Restore")
                        }
                        OutlinedButton(onClick = { onDelete(item) }) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderGrid(
    folders: Map<String, List<MediaItem>>,
    coverUris: Map<String, String?>,
    onFolderClick: (String, List<MediaItem>) -> Unit,
    onPhotoClick: (MediaItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        folders.forEach { (name, list) ->
            item(key = name) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f),
                    onClick = { onFolderClick(name, list) }
                ) {
                    Column(Modifier.padding(8.dp)) {
                        if (list.isNotEmpty()) {
                            val overrideCover = coverUris[name]
                            val coverModel = when {
                                !overrideCover.isNullOrBlank() -> Uri.parse(overrideCover)
                                else -> list.maxByOrNull { it.dateTaken }?.uri ?: list.first().uri
                            }
                            AsyncImage(
                                model = coverModel,
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = name.substringAfterLast("/").ifBlank { name },
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )
                        Text(
                            text = "${list.size} photo(s)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
