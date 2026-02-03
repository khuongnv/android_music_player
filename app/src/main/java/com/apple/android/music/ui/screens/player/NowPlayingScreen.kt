package com.apple.android.music.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.apple.android.music.R
import com.apple.android.music.ui.theme.AppleRed
import com.apple.android.music.ui.theme.MusicPlayerTheme
import com.apple.android.music.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onCollapse: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    // For slider smoothness
    var sliderPosition by remember { mutableStateOf<Float?>(null) }

    MusicPlayerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) 
        ) {
        // Blurred Background Overlay
        currentSong?.let { song ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.albumArtUri ?: R.drawable.ic_default_album_art)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.7f) // Increased vibrancy
                    .blur(70.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            )
            
            // Dynamic scrim based on theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Check currentSong validity
            if (currentSong == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No music playing")
                }
                return@Column
            }

            // Top Bar (Grabber)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                 IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.align(Alignment.CenterStart) 
                 ) {
                     Icon(
                         imageVector = Icons.Default.KeyboardArrowDown, 
                         contentDescription = "Collapse", 
                         modifier = Modifier.size(32.dp),
                         tint = MaterialTheme.colorScheme.onSurface
                     )
                 }
                 
                 Box(
                     modifier = Modifier
                         .width(40.dp)
                         .height(5.dp)
                         .clip(RoundedCornerShape(50))
                         .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                 )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(30.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentSong!!.albumArtUri ?: R.drawable.ic_default_album_art)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Artwork",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Title & Artist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong!!.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong!!.artist,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(onClick = { /* More Options */ }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "More", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderPosition ?: currentPosition.toFloat(),
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = {
                        sliderPosition?.let { viewModel.seekTo(it.toLong()) }
                        sliderPosition = null
                    },
                    valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = TimeUtils.formatTime(sliderPosition?.toLong() ?: currentPosition),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                    Text(
                        text = TimeUtils.formatTime(duration),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle (Optional implementation)
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                     Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                }
                
                IconButton(onClick = { viewModel.skipPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
                
                // Play/Pause - Big
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                   Icon(
                       imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                       contentDescription = "Play/Pause",
                       modifier = Modifier.size(36.dp),
                       tint = MaterialTheme.colorScheme.background // Inverted color
                   )
                }
                
                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
                
                // Repeat (Optional implementation)
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                     Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))
            
            // Volume Slider (Visual only for now, Apple Music has it)
            // Icon(Icons.AutoMirrored.Filled.VolumeDown) -- Slider -- Icon(Icons.AutoMirrored.Filled.VolumeUp)
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
}
