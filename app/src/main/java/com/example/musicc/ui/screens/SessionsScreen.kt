package com.example.musicc.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicc.data.room.PlaybackSessionEntity
import com.example.musicc.ui.theme.*
import com.example.musicc.viewmodel.SessionManagementViewModel

@Composable
fun SessionsScreen(
    viewModel: SessionManagementViewModel,
    onSessionSelected: (sessionId: Long) -> Unit = {}
) {
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreatingSession.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<PlaybackSessionEntity?>(null) }
    var newSessionName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BackgroundDark, BackgroundDarker)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(brush = Brush.linearGradient(colors = listOf(PrimaryPurple, PrimaryBlue, AccentCyan))),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Listening Sessions",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${allSessions.size} sessions • Create unlimited",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Error Message
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = ErrorRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                            Text("✕", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Sessions List or Empty State
            if (allSessions.isEmpty()) {
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
                            text = "No Sessions Yet",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first listening session\nby tapping the button below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allSessions) { session ->
                        ModernSessionCard(
                            session = session,
                            isActive = session.id == activeSession?.id,
                            onSelect = {
                                onSessionSelected(session.id)
                                viewModel.switchToSession(session.id, autoPlay = true)
                            },
                            onEdit = {
                                selectedSession = session
                                newSessionName = session.title ?: ""
                                showEditDialog = true
                            },
                            onDelete = { viewModel.deleteSession(session.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = PrimaryBlue,
            contentColor = TextPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create", modifier = Modifier.size(28.dp))
        }
    }

    // Create Session Dialog
    if (showCreateDialog) {
        ModernSessionDialog(
            title = "Create New Session",
            onDismiss = { showCreateDialog = false },
            onConfirm = { sessionName ->
                viewModel.createNewSession(sessionName)
                showCreateDialog = false
            },
            isLoading = isCreating
        )
    }

    // Edit Session Dialog
    if (showEditDialog && selectedSession != null) {
        ModernSessionDialog(
            title = "Rename Session",
            initialValue = newSessionName,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                selectedSession?.let { viewModel.renameSession(it.id, newName) }
                showEditDialog = false
                selectedSession = null
            },
            isLoading = false
        )
    }
}

@Composable
private fun ModernSessionCard(
    session: PlaybackSessionEntity,
    isActive: Boolean = false,
    onSelect: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val backgroundColor by animateColorAsState(targetValue = if (isActive) PrimaryBlue.copy(alpha = 0.15f) else SurfaceCard)
    val borderColor by animateColorAsState(targetValue = if (isActive) PrimaryBlue else SurfaceLight)
    val elevation by animateDpAsState(targetValue = if (isActive) 8.dp else 2.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isActive) {
                        Box(modifier = Modifier.size(10.dp).background(color = AccentCyan, shape = CircleShape))
                    }
                    Text(
                        text = session.title ?: "Untitled",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1
                    )
                    if (isActive) {
                        Surface(modifier = Modifier.clip(RoundedCornerShape(6.dp)), color = PrimaryBlue.copy(alpha = 0.3f)) {
                            Text(
                                text = "Playing",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AccentCyan,
                                modifier = Modifier.padding(4.dp, 2.dp)
                            )
                        }
                    }
                }
                Text(text = "Created ${formatDate(session.createdAt)}", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }

            if (!isActive) {
                Box(
                    modifier = Modifier.size(48.dp).background(color = PrimaryBlue.copy(alpha = 0.2f), shape = CircleShape).clickable { onSelect() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentCyan, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernSessionDialog(
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false
) {
    var sessionName by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
        text = {
            TextField(
                value = sessionName,
                onValueChange = { sessionName = it },
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
                onClick = { onConfirm(sessionName) },
                enabled = sessionName.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = TextPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary)
                } else {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
