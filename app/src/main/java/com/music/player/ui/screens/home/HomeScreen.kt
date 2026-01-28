package com.music.player.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.music.player.util.MediaScannerUtil
import com.music.player.ui.components.LargeAlbumCard
import com.music.player.ui.components.SongItem
import com.music.player.ui.components.MiniPlayer
import com.music.player.ui.theme.AppleRed

@Composable
fun HomeScreen(
    onAlbumClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Text(
                        text = "Loading music library...",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadHomeData() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.recentSongs.isEmpty() && uiState.recentAlbums.isEmpty() -> {
                    val context = LocalContext.current
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No music found in your library",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            MediaScannerUtil.scanMusicFolder(context)
                            viewModel.loadHomeData() 
                        }) {
                            Text("Scan for Music")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp) // Normal padding now
                    ) {
                        item {
                            Text(
                                text = "Listen Now",
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
                            )
                        }

                        // Recent Albums Section
                        if (uiState.recentAlbums.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Recently Played",
                                    onSeeAllClick = onSeeAllClick
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(uiState.recentAlbums, key = { it.id }) { album ->
                                        LargeAlbumCard(
                                            album = album,
                                            onClick = { onAlbumClick(album.id) }
                                        )
                                    }
                                }
                            }
                        }

                        // Recent Songs Section
                        if (uiState.recentSongs.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                SectionHeader(
                                    title = "New Releases",
                                    onSeeAllClick = onSeeAllClick
                                )
                            }
                            
                            items(uiState.recentSongs, key = { it.id }) { song ->
                                SongItem(
                                    song = song,
                                    isPlaying = isPlaying && currentSong?.id == song.id,
                                    onClick = { 
                                        viewModel.playSongs(uiState.recentSongs, uiState.recentSongs.indexOf(song))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        )
        Row(
            modifier = Modifier.clickable(onClick = onSeeAllClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
