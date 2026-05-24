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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
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
fun PlaylistDetailScreen(
    playlistId: Long,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    val playlist by viewModel.getPlaylistById(playlistId).collectAsStateWithLifecycle(initialValue = null)
    val playlistSongs by viewModel.getPlaylistSongs(playlistId).collectAsStateWithLifecycle(initialValue = emptyList())
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showPlaylistOptions by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAddSongsDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    
    var songForOptions by remember { mutableStateOf<Song?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updatePlaylistCover(playlistId, it) }
    }

    val filteredSongs = if (searchQuery.isEmpty()) {
        playlistSongs
    } else {
        playlistSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BackgroundDark, BackgroundDarker)))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            if (isSearching) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search in playlist...", color = TextSecondary) },
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
                    text = playlist?.name ?: "Playlist",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    maxLines = 1
                )
                
                Row {
                    IconButton(onClick = { isSearching = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                    IconButton(onClick = { showPlaylistOptions = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextPrimary)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Info
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playlist?.customCoverUri != null) {
                            AsyncImage(
                                model = playlist?.customCoverUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = PrimaryPurple.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { playlist?.let { viewModel.playPlaylist(it, false) } },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Play All")
                        }
                        
                        OutlinedButton(
                            onClick = { playlist?.let { viewModel.playPlaylist(it, true) } },
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(PrimaryBlue, AccentCyan))),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Shuffle", color = AccentCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showAddSongsDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Songs", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Songs List
            items(filteredSongs) { song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onOptionsClick = { songForOptions = song }
                )
            }
        }
    }

    // Playlist Options Sheet
    if (showPlaylistOptions) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistOptions = false },
            containerColor = SurfaceCard,
            contentColor = TextPrimary
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Play Next") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.playPlaylistNext(playlistId)
                        showPlaylistOptions = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Add Songs") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showAddSongsDialog = true
                        showPlaylistOptions = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Change Cover") },
                    leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                    modifier = Modifier.clickable {
                        imageLauncher.launch("image/*")
                        showPlaylistOptions = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Rename") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        newPlaylistName = playlist?.name ?: ""
                        showRenameDialog = true
                        showPlaylistOptions = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Delete Playlist", color = ErrorRed) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                    modifier = Modifier.clickable {
                        playlist?.let { viewModel.deletePlaylist(it) }
                        onBackClick()
                        showPlaylistOptions = false
                    }
                )
            }
        }
    }

    // Song context menu (specific to playlist)
    songForOptions?.let { song ->
        ModalBottomSheet(
            onDismissRequest = { songForOptions = null },
            containerColor = SurfaceCard,
            contentColor = TextPrimary
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Remove from Playlist") },
                    leadingContent = { Icon(Icons.Default.RemoveCircleOutline, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.removeSongFromPlaylist(playlistId, song.id)
                        songForOptions = null
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
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Playlist") },
            text = {
                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundDark,
                        unfocusedContainerColor = BackgroundDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        viewModel.renamePlaylist(playlistId, newPlaylistName)
                        showRenameDialog = false
                    }
                }) { Text("Save", color = PrimaryBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            },
            containerColor = SurfaceCard
        )
    }

    // Add Songs Dialog
    if (showAddSongsDialog) {
        AlertDialog(
            onDismissRequest = { showAddSongsDialog = false },
            title = { Text("Add Songs") },
            text = {
                Box(modifier = Modifier.height(450.dp)) {
                    LazyColumn {
                        items(allSongs) { song ->
                            val isAlreadyIn = playlistSongs.any { it.id == song.id }
                            ListItem(
                                headlineContent = { Text(song.title, color = if (isAlreadyIn) TextTertiary else TextPrimary) },
                                supportingContent = { Text(song.artist, color = TextTertiary) },
                                leadingContent = {
                                    AsyncImage(
                                        model = song.albumArtUri,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                trailingContent = {
                                    if (isAlreadyIn) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                                    } else {
                                        IconButton(onClick = { viewModel.addSongToPlaylist(playlistId, song) }) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue)
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = BackgroundDark)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddSongsDialog = false }) { Text("Done", color = PrimaryBlue, fontWeight = FontWeight.Bold) }
            },
            containerColor = SurfaceCard
        )
    }
}
