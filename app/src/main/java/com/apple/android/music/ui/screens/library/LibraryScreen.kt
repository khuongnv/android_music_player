package com.apple.android.music.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apple.android.music.ui.screens.albums.AlbumGridItem
import com.apple.android.music.ui.theme.AppleRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateTo: (String) -> Unit,
    onAlbumClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp
                    ),
                    modifier = Modifier.padding(20.dp)
                )
            }

            // Menu Items
            val menuItems = listOf(
                Triple("Playlists", Icons.AutoMirrored.Filled.QueueMusic, "playlists"),
                Triple("Artists", Icons.Default.Person, "artists"),
                Triple("Albums", Icons.Default.Album, "albums"),
                Triple("Songs", Icons.Default.MusicNote, "songs")
            )
            
            items(menuItems) { (title, icon, route) ->
                LibraryMenuItem(
                    title = title,
                    icon = icon,
                    onClick = { onNavigateTo(route) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp), // Indent divider to align with text
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
            }

            // Recently Added
            if (uiState.recentAlbums.isNotEmpty()) {
                item {
                    Text(
                        text = "Recently Added",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 20.dp, top = 30.dp, bottom = 15.dp)
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        items(uiState.recentAlbums) { album ->
                            AlbumGridItem(album = album, onClick = { onAlbumClick(album.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppleRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    }
}
