package com.apple.android.music.ui.screens.songs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apple.android.music.ui.components.SongItem
import com.apple.android.music.ui.theme.AppleRed
import com.apple.android.music.ui.theme.DarkBackground
import com.apple.android.music.ui.theme.Gray93

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    onBackClick: () -> Unit,
    viewModel: SongsViewModel = hiltViewModel()
) {
    val uiState by viewModel.songsState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    val glassBg = if (isDark) Color(0xEE1E1E22) else Color(0xF2FFFFFF)
    val glassBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Bài hát",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (uiState.songs.isNotEmpty()) {
                            Text(
                                "${uiState.songs.size} bài hát",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray93
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Liquid Glass Twin Control Pills (Play All & Shuffle)
                if (uiState.songs.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LiquidGlassActionPill(
                                icon = Icons.Default.PlayArrow,
                                label = "Phát tất cả",
                                glassBg = glassBg,
                                glassBorder = glassBorder,
                                isDark = isDark,
                                onClick = {
                                    uiState.songs.firstOrNull()?.let { viewModel.playSong(it) }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            LiquidGlassActionPill(
                                icon = Icons.Default.Shuffle,
                                label = "Trộn bài",
                                glassBg = glassBg,
                                glassBorder = glassBorder,
                                isDark = isDark,
                                onClick = { viewModel.shuffleAll() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

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

@Composable
private fun LiquidGlassActionPill(
    icon: ImageVector,
    label: String,
    glassBg: Color,
    glassBorder: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = if (isDark) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isDark) Color(0x40000000) else Color(0x1A000000)
            )
            .border(
                BorderStroke(0.75.dp, glassBorder),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = glassBg,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppleRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = AppleRed
            )
        }
    }
}
