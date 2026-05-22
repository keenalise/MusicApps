package com.example.musicc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicc.data.room.PlaybackSessionEntity
import com.example.musicc.ui.theme.SpotifyGreen
import com.example.musicc.ui.theme.SpotifyGray
import com.example.musicc.ui.theme.SpotifyLightGray
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sessions",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = SpotifyGreen,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Session")
            }
        }

        // Error message
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Sessions List
        if (allSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sessions yet.\nTap + to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpotifyLightGray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allSessions) { session ->
                    SessionCard(
                        session = session,
                        isActive = session.id == activeSession?.id,
                        onSelect = {
                            // Switch to session with autoPlay enabled
                            viewModel.switchToSession(session.id, autoPlay = true)
                            onSessionSelected(session.id)
                        },
                        onEdit = {
                            selectedSession = session
                            newSessionName = session.title ?: ""
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteSession(session.id)
                        }
                    )
                }
            }
        }
    }

    // Create Session Dialog
    if (showCreateDialog) {
        CreateSessionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { sessionName ->
                viewModel.createNewSession(sessionName)
                newSessionName = ""
                showCreateDialog = false
            },
            isLoading = isCreating
        )
    }

    // Edit Session Dialog
    if (showEditDialog && selectedSession != null) {
        EditSessionDialog(
            sessionName = newSessionName,
            onNameChange = { newSessionName = it },
            onDismiss = { showEditDialog = false },
            onConfirm = {
                selectedSession?.let {
                    viewModel.renameSession(it.id, newSessionName)
                }
                showEditDialog = false
                selectedSession = null
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: PlaybackSessionEntity,
    isActive: Boolean = false,
    onSelect: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) SpotifyGreen.copy(alpha = 0.2f) else SpotifyGray
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) SpotifyGreen.copy(alpha = 0.2f) else SpotifyGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.title ?: "Untitled Session",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (isActive) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp)),
                            color = SpotifyGreen
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Created: ${formatTime(session.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpotifyLightGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Session",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false
) {
    var sessionName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Session")
        },
        text = {
            TextField(
                value = sessionName,
                onValueChange = { sessionName = it },
                label = { Text("Session Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(sessionName) },
                enabled = !isLoading && sessionName.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditSessionDialog(
    sessionName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Session")
        },
        text = {
            TextField(
                value = sessionName,
                onValueChange = onNameChange,
                label = { Text("Session Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = sessionName.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
}

