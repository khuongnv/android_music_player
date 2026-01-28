package com.music.player.ui.screens.songs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.music.player.ui.components.SongItem
import com.music.player.ui.theme.AppleRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    onBackClick: () -> Unit,
    viewModel: SongsViewModel = hiltViewModel()
) {
    val uiState by viewModel.songsState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Songs") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shuffleAll() }) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = AppleRed)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp) // MiniPlayer spacing
            ) {
                // Shuffle Button Row (iOS style often has a big shuffle button at top, or just use App Bar action)
                // We'll use App Bar action for now as per Material guidelines but can add a Play/Shuffle buttons row if preferred.
                
                items(uiState.songs, key = { it.id }) { song ->
                    SongItem(
                        song = song,
                        isPlaying = isPlaying && currentSong?.id == song.id,
                        onClick = { viewModel.playSong(song) }
                    )
                }
            }
        }
    }
}
