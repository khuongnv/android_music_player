package com.apple.android.music.ui.screens.nowplaying

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.apple.android.music.ui.theme.AppleRed
import com.apple.android.music.ui.theme.Gray93
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.positionFlow.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    // Animation state for album art scale on play/pause
    val albumScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.85f,
        label = "AlbumScale"
    )

    // Gesture state for swipe down to collapse
    var offsetY by remember { mutableStateOf(0f) }
    val configuration = LocalConfiguration.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    if (delta > 0 || offsetY > 0) {
                        offsetY += delta
                    }
                },
                onDragStopped = {
                    if (offsetY > 300) {
                        onBackClick()
                    } else {
                        offsetY = 0f
                    }
                }
            )
            .background(Color.Black)
    ) {
        // Dynamic Background Gradient (Tints based on Album Art - mocked here)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF333333),
                            Color.Black
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle (iOS style)
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
            
            Spacer(modifier = Modifier.height(60.dp))

            // Large Album Artwork with Parallax and Scale effect
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .scale(albumScale)
                    .shadow(
                        elevation = 40.dp,
                        shape = RoundedCornerShape(12.dp),
                        clip = false,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = currentSong?.albumArtUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.res.painterResource(com.apple.android.music.R.drawable.ic_default_album_art),
                    error = androidx.compose.ui.res.painterResource(com.apple.android.music.R.drawable.ic_default_album_art),
                    fallback = androidx.compose.ui.res.painterResource(com.apple.android.music.R.drawable.ic_default_album_art),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Song Info - Centered like Apple Music
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong?.title ?: "Not Playing",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong?.artist ?: "Unknown Artist",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    IconButton(onClick = { /* Options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)).padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar - Apple Style
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
                    onValueChange = { scale ->
                        viewModel.seekTo((scale * duration).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(position),
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                    )
                    Text(
                        text = "-${formatDuration(duration - position)}",
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Playback Controls - Large and Iconic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::skipPrevious, modifier = Modifier.size(64.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                IconButton(onClick = viewModel::togglePlayPause, modifier = Modifier.size(96.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                }

                IconButton(onClick = viewModel::skipNext, modifier = Modifier.size(64.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Tools (Volume/Lyrics mock buttons)
            Row(
                modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VolumeDown, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                // Volume slider mock
                Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 12.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(2.dp)))
                Icon(Icons.Default.VolumeUp, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = viewModel::toggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleEnabled) AppleRed else Color.White.copy(alpha = 0.4f)
                    )
                }
                
                IconButton(onClick = { /* Airplay/Cast */ }) {
                    Icon(
                        imageVector = Icons.Default.Airplay,
                        contentDescription = "Airplay",
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
                
                IconButton(onClick = viewModel::toggleRepeat) {
                    Icon(
                        imageVector = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) AppleRed else Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

private fun formatDuration(duration: Long): String {
    val totalSeconds = (duration.coerceAtLeast(0) / 1000)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
