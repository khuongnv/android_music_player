package com.apple.android.music.ui.screens.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.apple.android.music.domain.model.Album
import com.apple.android.music.ui.theme.AppleRed
import com.apple.android.music.ui.theme.DarkBackground
import com.apple.android.music.ui.theme.Gray93

/**
 * Liquid Glass Library Screen:
 * - Floating glass stats pills (Songs / Albums / Artists counters)
 * - Signature Liquid Glass Navigation Group Card with glowing capsule icons
 * - Liquid Glass Recently Added album carousel with frosted glass frames
 */
@Composable
fun LibraryScreen(
    onNavigateTo: (String) -> Unit,
    onAlbumClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    // Liquid Glass color tokens
    val glassBg = if (isDark) {
        Color(0xEE1E1E22) // Translucent smoky dark glass
    } else {
        Color(0xF2FFFFFF) // Crystalline translucent white glass
    }

    val glassBorder = if (isDark) {
        Color(0x33FFFFFF) // Luminous silver rim
    } else {
        Color(0x1F000000) // Subtle shadow rim
    }

    Scaffold(
        topBar = { },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Apple Music Large Page Title
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "Thư viện",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Âm nhạc offline và danh sách phát trên thiết bị",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray93
                    )
                }
            }

            // --- 1. Liquid Glass Quick Stats Pills ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassStatPill(
                        icon = Icons.Default.MusicNote,
                        count = uiState.songs.size,
                        label = "Bài hát",
                        glassBg = glassBg,
                        glassBorder = glassBorder,
                        isDark = isDark,
                        onClick = { onNavigateTo("songs") },
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassStatPill(
                        icon = Icons.Default.Album,
                        count = uiState.albums.size,
                        label = "Album",
                        glassBg = glassBg,
                        glassBorder = glassBorder,
                        isDark = isDark,
                        onClick = { onNavigateTo("albums") },
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassStatPill(
                        icon = Icons.Default.Person,
                        count = uiState.artists.size,
                        label = "Nghệ sĩ",
                        glassBg = glassBg,
                        glassBorder = glassBorder,
                        isDark = isDark,
                        onClick = { onNavigateTo("artists") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // --- 2. Signature Liquid Glass Navigation Menu Card ---
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = if (isDark) 16.dp else 10.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = if (isDark) Color(0x66000000) else Color(0x26000000)
                        )
                        .border(
                            BorderStroke(0.75.dp, glassBorder),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = glassBg,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        val navItems = listOf(
                            Triple("Danh sách phát", Icons.AutoMirrored.Filled.QueueMusic, "playlists") to "${uiState.playlists.size} danh sách",
                            Triple("Nghệ sĩ", Icons.Default.Person, "artists") to "${uiState.artists.size} nghệ sĩ",
                            Triple("Album", Icons.Default.Album, "albums") to "${uiState.albums.size} album",
                            Triple("Bài hát", Icons.Default.MusicNote, "songs") to "${uiState.songs.size} bài hát"
                        )

                        navItems.forEachIndexed { index, (itemData, countText) ->
                            val (title, icon, route) = itemData

                            LiquidGlassMenuItem(
                                title = title,
                                subtitle = countText,
                                icon = icon,
                                onClick = { onNavigateTo(route) }
                            )

                            if (index < navItems.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                                    thickness = 0.5.dp,
                                    color = if (isDark) Color(0x1AFFFFFF) else Color(0x14000000)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(26.dp))
            }

            // --- 3. Liquid Glass Recently Added Section ---
            if (uiState.recentAlbums.isNotEmpty()) {
                item {
                    Text(
                        text = "ĐÃ THÊM GẦN ĐÂY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 12.sp
                        ),
                        color = Gray93,
                        modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.recentAlbums) { album ->
                            LiquidGlassAlbumCard(
                                album = album,
                                glassBg = glassBg,
                                glassBorder = glassBorder,
                                isDark = isDark,
                                onClick = { onAlbumClick(album.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick Metric Stat Pill in Liquid Glass style
 */
@Composable
private fun LiquidGlassStatPill(
    icon: ImageVector,
    count: Int,
    label: String,
    glassBg: Color,
    glassBorder: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 8.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isDark) Color(0x40000000) else Color(0x1F000000)
            )
            .border(
                BorderStroke(0.75.dp, glassBorder),
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = glassBg,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppleRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp
                ),
                color = Gray93
            )
        }
    }
}

/**
 * Menu Item inside the Liquid Glass Group Card
 */
@Composable
private fun LiquidGlassMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Glowing Icon Capsule
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppleRed.copy(alpha = 0.12f))
                    .border(
                        BorderStroke(0.5.dp, AppleRed.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppleRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Gray93
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gray93.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Liquid Glass Album Card for Recently Added Carousel
 */
@Composable
private fun LiquidGlassAlbumCard(
    album: Album,
    glassBg: Color,
    glassBorder: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(156.dp)
            .shadow(
                elevation = if (isDark) 12.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isDark) Color(0x55000000) else Color(0x20000000)
            )
            .border(
                BorderStroke(0.75.dp, glassBorder),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = glassBg,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Artwork with smooth corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = album.albumArtUri,
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.res.painterResource(com.apple.android.music.R.drawable.ic_default_album_art),
                    error = androidx.compose.ui.res.painterResource(com.apple.android.music.R.drawable.ic_default_album_art),
                    fallback = androidx.compose.ui.res.painterResource(com.apple.android.music.R.drawable.ic_default_album_art),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = album.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp
                ),
                color = Gray93,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
