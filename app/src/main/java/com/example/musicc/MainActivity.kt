package com.example.musicc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.musicc.model.Song
import com.example.musicc.ui.navigation.Screen
import com.example.musicc.ui.navigation.bottomNavItems
import com.example.musicc.ui.screens.HomeScreen
import com.example.musicc.ui.screens.LibraryScreen
import com.example.musicc.ui.screens.PlayerScreen
import com.example.musicc.ui.screens.SessionsScreen
import com.example.musicc.ui.theme.MusiccTheme
import com.example.musicc.ui.theme.SurfaceCard

import com.example.musicc.viewmodel.MusicViewModel
import com.example.musicc.viewmodel.SessionManagementViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusiccTheme {
                MusicApp(viewModel)
            }
        }
    }
}

@Composable
fun MusicApp(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sessionViewModel: SessionManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    var showPlayer by remember { mutableStateOf(false) }

    // Collect states from ViewModel
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()

    // Track current position for progress bar
    var currentPosition by remember { mutableStateOf(0L) }

    // Update position every second when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = viewModel.getCurrentPosition()
            delay(1000)
        }
    }

    // Permission handling
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadSongs()
        }
    }

    // Request permission and load songs
    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.loadSongs()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                // Mini Player
                currentSong?.let { song ->
                    if (!showPlayer) {
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            onClick = { showPlayer = true },
                            onPlayPauseClick = { viewModel.togglePlayPause() }
                        )
                    }
                }

                // Bottom Navigation
                if (!showPlayer) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        bottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    indicatorColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showPlayer && currentSong != null) {
            PlayerScreen(
                song = currentSong!!,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
                onBackClick = { showPlayer = false },
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipToNext() },
                onSkipPrevious = { viewModel.skipToPrevious() },
                onSeek = { position -> viewModel.seekTo(position) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() }
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        songs = allSongs,
                        isLoading = isLoading,
                        onSongClick = { song ->
                            viewModel.playSong(song)
                            showPlayer = true
                        }
                    )
                }
                composable(Screen.Library.route) {
                    LibraryScreen()
                }
                composable(Screen.Sessions.route) {
                    SessionsScreen(
                        viewModel = sessionViewModel,
                        onSessionSelected = { sessionId ->
                            // SessionManager automatically saves current position before switching
                            sessionViewModel.switchToSession(
                                sessionId = sessionId,
                                autoPlay = isPlaying
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art placeholder
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }

        IconButton(onClick = { /* Toggle favorite */ }) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
