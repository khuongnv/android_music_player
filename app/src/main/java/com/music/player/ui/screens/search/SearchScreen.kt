package com.music.player.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.music.player.ui.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        // Apple style search bar
        TextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(10.dp)),
            placeholder = { Text("Artists, Songs, Lyrics, and More") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (uiState.songs.isNotEmpty()) {
                item {
                    SectionHeader(title = "Songs")
                }
                items(uiState.songs) { song ->
                    SongItem(
                        song = song,
                        isPlaying = isPlaying && currentSong?.id == song.id,
                        onClick = { viewModel.playSong(song) }
                    )
                }
            }

            if (uiState.albums.isNotEmpty()) {
                item {
                    SectionHeader(title = "Albums")
                }
                items(uiState.albums) { album ->
                    AlbumSearchItem(
                        name = album.name,
                        artist = album.artist,
                        onClick = { onAlbumClick(album.id) }
                    )
                }
            }

            if (uiState.artists.isNotEmpty()) {
                item {
                    SectionHeader(title = "Artists")
                }
                items(uiState.artists) { artist ->
                    ArtistSearchItem(
                        name = artist.name,
                        onClick = { onArtistClick(artist.name) }
                    )
                }
            }
            
            if (uiState.query.isNotEmpty() && uiState.songs.isEmpty() && !uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                        Text("No results for \"${uiState.query}\"")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
fun AlbumSearchItem(name: String, artist: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Simple placeholder for now
        Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)).background(Color.Gray))
        Spacer(modifier = Modifier.width(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = artist, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)) {
            Text("View")
        }
    }
}

@Composable
fun ArtistSearchItem(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle for artists
        Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(25.dp)).background(Color.LightGray))
        Spacer(modifier = Modifier.width(15.dp))
        Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)) {
            Text("View")
        }
    }
}
