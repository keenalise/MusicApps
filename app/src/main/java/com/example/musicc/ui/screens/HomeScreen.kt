package com.example.musicc.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.musicc.model.Song
import com.example.musicc.ui.theme.*
import com.example.musicc.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onSongClick: (Song) -> Unit = {}
) {
    val songs by viewModel.allSongs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Dialog states
    var songForOptions by remember { mutableStateOf<Song?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            songForOptions?.let { song ->
                viewModel.updateCover(song, selectedUri)
            }
        }
    }

    val filteredSongs = if (searchQuery.isEmpty()) {
        songs
    } else {
        songs.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.artist.contains(searchQuery, ignoreCase = true) 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BackgroundDark, BackgroundDarker)))
    ) {
        // Top Bar with Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearching) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search songs...", color = TextSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { 
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Text(
                    text = "My Music",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                IconButton(
                    onClick = { isSearching = true },
                    modifier = Modifier
                        .background(SurfaceCard, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            } else if (filteredSongs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No songs found." else "No results for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(filteredSongs) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onOptionsClick = { songForOptions = song }
                    )
                }
            }
        }
    }

    // Song Options Menu (Context Menu)
    songForOptions?.let { song ->
        ModalBottomSheet(
            onDismissRequest = { 
                songForOptions = null 
            },
            containerColor = SurfaceCard,
            contentColor = TextPrimary
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Rename") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        newTitle = song.title
                        showRenameDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text("Change Cover") },
                    leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                    modifier = Modifier.clickable {
                        imageLauncher.launch("image/*")
                        songForOptions = null
                    }
                )
                ListItem(
                    headlineContent = { Text("Add to Playlist") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showPlaylistDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text(if (song.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                    leadingContent = { 
                        Icon(
                            if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (song.isFavorite) PrimaryBlue else TextPrimary
                        ) 
                    },
                    modifier = Modifier.clickable {
                        viewModel.toggleFavorite(song)
                        songForOptions = null
                    }
                )
                ListItem(
                    headlineContent = { Text("Share") },
                    leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.shareSong(song)
                        songForOptions = null
                    }
                )
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Song") },
            text = {
                TextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(focusedContainerColor = BackgroundDark)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    songForOptions?.let { viewModel.renameSong(it, newTitle) }
                    showRenameDialog = false
                    songForOptions = null
                }) { Text("Save", color = PrimaryBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            },
            containerColor = SurfaceCard
        )
    }

    // Playlist Dialog
    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                Column {
                    if (playlists.isEmpty()) {
                        Text("No playlists found.", color = TextSecondary)
                    }
                    playlists.forEach { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            modifier = Modifier.clickable {
                                songForOptions?.let { viewModel.addSongToPlaylist(playlist.id, it) }
                                showPlaylistDialog = false
                                songForOptions = null
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showPlaylistDialog = false
                    songForOptions = null
                }) { Text("Close") }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (song.isFavorite) PrimaryBlue else TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1
            )
        }

        IconButton(onClick = onOptionsClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary)
        }
    }
}
