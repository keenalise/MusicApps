package com.example.musicc.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicc.model.Song
import com.example.musicc.ui.theme.*
import com.example.musicc.viewmodel.MusicViewModel
import com.example.musicc.viewmodel.SessionManagementViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateSessionFromLibraryScreen(
    musicViewModel: MusicViewModel,
    sessionViewModel: SessionManagementViewModel,
    onSessionCreated: (Long) -> Unit = {}
) {
    val allSongs by musicViewModel.allSongs.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedSongs by remember { mutableStateOf(setOf<Song>()) }
    var sessionTitle by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BackgroundDark, BackgroundDarker)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(brush = Brush.linearGradient(colors = listOf(PrimaryPurple, PrimaryBlue, AccentCyan))),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Create Session",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${selectedSongs.size} songs selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showDialog = true },
                    enabled = selectedSongs.isNotEmpty() && !isCreating,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create", fontWeight = FontWeight.Bold)
                }

                if (selectedSongs.isNotEmpty()) {
                    Button(
                        onClick = { selectedSongs = emptySet() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear")
                    }
                }
            }

            // Songs List
            if (allSongs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(color = PrimaryPurple.copy(alpha = 0.2f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎵", style = MaterialTheme.typography.displayMedium)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Songs Found",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Load songs from library first",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allSongs) { song ->
                        val isSelected = song in selectedSongs
                        SongSelectionCard(
                            song = song,
                            isSelected = isSelected,
                            onClick = {
                                selectedSongs = if (isSelected) {
                                    selectedSongs - song
                                } else {
                                    selectedSongs + song
                                }
                            }
                        )
                    }
                }
            }
        }

        // Create Session Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = SurfaceCard,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary,
                title = { Text("Create Session with ${selectedSongs.size} songs", fontWeight = FontWeight.Bold) },
                text = {
                    TextField(
                        value = sessionTitle,
                        onValueChange = { sessionTitle = it },
                        label = { Text("Session name") },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = SurfaceLight
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (sessionTitle.isNotBlank()) {
                                isCreating = true
                                scope.launch {
                                    try {
                                        sessionViewModel.createNewSession(sessionTitle)
                                        showDialog = false
                                        selectedSongs = emptySet()
                                        sessionTitle = ""
                                        onSessionCreated(-1)
                                    } finally {
                                        isCreating = false
                                    }
                                }
                            }
                        },
                        enabled = sessionTitle.isNotBlank() && !isCreating,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary)
                        } else {
                            Text("Create", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SongSelectionCard(
    song: Song,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else SurfaceCard
    val borderColor = if (isSelected) PrimaryBlue else SurfaceLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue, uncheckedColor = TextTertiary)
            )
        }
    }
}

