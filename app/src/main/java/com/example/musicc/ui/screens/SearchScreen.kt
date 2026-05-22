package com.example.musicc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.musicc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BackgroundDark, BackgroundDarker)))
            .padding(16.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            placeholder = {
                Text(
                    text = "Artists, songs, or albums",
                    color = TextTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextTertiary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = SurfaceLight
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Browse all",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Browse Categories Grid
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(6) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrowseCategory(
                        title = when(index * 2) {
                            0 -> "Pop"
                            2 -> "Hip-Hop"
                            4 -> "Rock"
                            6 -> "Jazz"
                            8 -> "Electronic"
                            else -> "Classical"
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = listOf(PrimaryPurple, AccentPink)
                    )
                    BrowseCategory(
                        title = when(index * 2 + 1) {
                            1 -> "R&B"
                            3 -> "Country"
                            5 -> "Latin"
                            7 -> "Blues"
                            9 -> "Indie"
                            else -> "Folk"
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = listOf(PrimaryBlue, AccentCyan)
                    )
                }
            }
        }
    }
}

@Composable
fun BrowseCategory(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: List<androidx.compose.ui.graphics.Color> = listOf(PrimaryPurple, AccentPink)
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(colors = backgroundColor)
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary
        )
    }
}

