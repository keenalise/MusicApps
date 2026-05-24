package com.example.musicc.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Library : Screen("library", "Your Library", Icons.Default.LibraryMusic)
    object Sessions : Screen("sessions", "Sessions", Icons.AutoMirrored.Filled.QueueMusic)
    object PlaylistDetail : Screen("playlist/{playlistId}", "Playlist", Icons.Default.LibraryMusic) {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Sessions
)
