package com.apple.android.music.ui.screens.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.apple.android.music.domain.model.PlaybackActivity
import com.apple.android.music.ui.theme.AppleRed
import com.apple.android.music.ui.theme.Gray93
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show feedback snackbar
    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        },
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
                Text(
                    text = "Hoạt động 24h",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp
                    ),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
                )
            }

            // --- 24H Overview Summary Card ---
            item {
                TimelineOverviewCard(
                    totalListeningTimeMs = uiState.totalListeningTimeMs,
                    activeSessionCount = uiState.activeSessionCount
                )
            }

            // --- Interactive 24-Hour Timeline Bar & Scrubber ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DÒNG THỜI GIAN TƯƠNG TÁC (24H)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        fontSize = 12.sp
                    ),
                    color = Gray93,
                    modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                )

                InteractiveTimelineBar(
                    activities = uiState.activities,
                    timelineStart = uiState.timelineStartTimestamp,
                    timelineEnd = uiState.timelineEndTimestamp,
                    scrubFraction = uiState.scrubFraction,
                    selectedTimestamp = uiState.selectedTimestamp,
                    isActiveAtCursor = uiState.isActiveAtCursor,
                    onScrubChanged = { viewModel.onScrubFractionChanged(it) }
                )
            }

            // --- Inspector Card for Selected Point ---
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SelectedPointInspectorCard(
                    isActive = uiState.isActiveAtCursor,
                    activity = uiState.activeItemAtCursor,
                    positionMs = uiState.positionAtCursorMs,
                    selectedTimestamp = uiState.selectedTimestamp,
                    onPlayFromHere = { viewModel.playAtSelectedPoint() }
                )
            }

            // --- Recent 24h Sessions Header ---
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "CÁC PHIÊN PHÁT NHẠC GẦN ĐÂY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        fontSize = 12.sp
                    ),
                    color = Gray93,
                    modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                )
            }

            // --- Sessions List ---
            if (uiState.activities.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có dữ liệu hoạt động trong 24 giờ qua.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray93
                            )
                        }
                    }
                }
            } else {
                items(uiState.activities.reversed()) { activity ->
                    ActivitySessionItem(
                        activity = activity,
                        onPlayClick = { viewModel.playSpecificActivity(activity) }
                    )
                }
            }
        }
    }
}

// ==========================================
// OVERVIEW SUMMARY CARD
// ==========================================

@Composable
private fun TimelineOverviewCard(
    totalListeningTimeMs: Long,
    activeSessionCount: Int
) {
    val totalMinutes = totalListeningTimeMs / 60000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val formattedDuration = if (hours > 0) "${hours} giờ ${minutes} phút" else "${minutes} phút"
    val percentOfDay = String.format(Locale.US, "%.1f%%", (totalListeningTimeMs.toDouble() / (24 * 3600 * 1000L)) * 100.0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tổng thời gian nghe (24h)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray93
                    )
                    Text(
                        text = formattedDuration,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppleRed
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppleRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = percentOfDay,
                        color = AppleRed,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tổng số lượt phát nhạc:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray93
                )
                Text(
                    text = "$activeSessionCount bài hát",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ==========================================
// INTERACTIVE TIMELINE BAR & SCRUBBER
// ==========================================

@Composable
private fun InteractiveTimelineBar(
    activities: List<PlaybackActivity>,
    timelineStart: Long,
    timelineEnd: Long,
    scrubFraction: Float,
    selectedTimestamp: Long,
    isActiveAtCursor: Boolean,
    onScrubChanged: (Float) -> Unit
) {
    val totalSpan = (timelineEnd - timelineStart).coerceAtLeast(1L)
    val fullDateFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val barWidth = maxWidth
        val density = LocalDensity.current

        Column(modifier = Modifier.fillMaxWidth()) {
            // Floating Tooltip Bubble above cursor
            val tooltipOffset = (barWidth * scrubFraction) - 45.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActiveAtCursor) AppleRed else MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 4.dp,
                    modifier = Modifier.offset {
                        IntOffset(
                            x = with(density) { tooltipOffset.coerceIn(0.dp, barWidth - 90.dp).roundToPx() },
                            y = 0
                        )
                    }
                ) {
                    Text(
                        text = fullDateFormatter.format(Date(selectedTimestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Timeline Track Container with Touch & Drag Handlers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            onScrubChanged(fraction)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            onScrubChanged(fraction)
                        }
                    }
            ) {
                // Inactive Background Subtle Texture Lines
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(24) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        )
                    }
                }

                // Render Active Playback Blocks along the 24h Axis
                activities.forEach { act ->
                    val blockStartFrac = ((act.startTime - timelineStart).toFloat() / totalSpan).coerceIn(0f, 1f)
                    val blockEndFrac = ((act.endTime - timelineStart).toFloat() / totalSpan).coerceIn(0f, 1f)
                    val widthFrac = (blockEndFrac - blockStartFrac).coerceAtLeast(0.005f) // min visible width

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(widthFrac)
                            .offset {
                                IntOffset(
                                    x = with(density) { (barWidth * blockStartFrac).roundToPx() },
                                    y = 0
                                )
                            }
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFF5B79), AppleRed)
                                )
                            )
                    )
                }

                // Draggable Scrubber Cursor Line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .offset {
                            IntOffset(
                                x = with(density) { (barWidth * scrubFraction).roundToPx() },
                                y = 0
                            )
                        }
                        .background(MaterialTheme.colorScheme.onSurface)
                )

                // Scrubber Knob (Indicator Handle)
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .offset {
                            IntOffset(
                                x = with(density) { (barWidth * scrubFraction - 9.dp).roundToPx() },
                                y = with(density) { 23.dp.roundToPx() }
                            )
                        }
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AppleRed)
                    )
                }
            }

            // Timeline Scale Labels (24h ago -> Now)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "-24h", style = MaterialTheme.typography.labelSmall, color = Gray93)
                Text(text = "-18h", style = MaterialTheme.typography.labelSmall, color = Gray93)
                Text(text = "-12h", style = MaterialTheme.typography.labelSmall, color = Gray93)
                Text(text = "-6h", style = MaterialTheme.typography.labelSmall, color = Gray93)
                Text(text = "Hiện tại", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AppleRed)
            }
        }
    }
}

// ==========================================
// INSPECTOR CARD AT SELECTED POINT
// ==========================================

@Composable
private fun SelectedPointInspectorCard(
    isActive: Boolean,
    activity: PlaybackActivity?,
    positionMs: Long,
    selectedTimestamp: Long,
    onPlayFromHere: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss • dd/MM", Locale.getDefault()) }
    val formattedTime = dateFormatter.format(Date(selectedTimestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) AppleRed else Gray93)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isActive) "TRẠNG THÁI: ĐANG PHÁT NHẠC" else "TRẠNG THÁI: KHÔNG HOẠT ĐỘNG",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isActive) AppleRed else Gray93
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray93
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(14.dp))

            if (isActive && activity != null) {
                // Active Song Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork Thumbnail
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(activity.artworkUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.songTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${activity.artistName} • ${activity.albumName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray93,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        // Position indicator
                        val posMins = (positionMs / 1000) / 60
                        val posSecs = (positionMs / 1000) % 60
                        val durMins = (activity.songDuration / 1000) / 60
                        val durSecs = (activity.songDuration / 1000) % 60
                        val timeStr = String.format(
                            Locale.US,
                            "%02d:%02d / %02d:%02d",
                            posMins, posSecs, durMins, durSecs
                        )

                        Text(
                            text = "Vị trí trong bài: $timeStr",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = AppleRed,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar within song
                val fraction = if (activity.songDuration > 0) {
                    (positionMs.toFloat() / activity.songDuration).coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AppleRed,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button: Play from here!
                Button(
                    onClick = onPlayFromHere,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val posMins = (positionMs / 1000) / 60
                    val posSecs = (positionMs / 1000) % 60
                    Text(
                        text = String.format(Locale.US, "Phát từ đoạn này (%02d:%02d)", posMins, posSecs),
                        fontWeight = FontWeight.Bold
                    )
                }

            } else {
                // Inactive State Card Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = Gray93,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Không có hoạt động phát nhạc",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ứng dụng đang tạm dừng hoặc thiết bị không phát nhạc vào thời điểm này.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray93,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Kéo con trỏ tới các vùng màu đỏ để xem bài hát đã phát.",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppleRed.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ==========================================
// RECENT ACTIVITY SESSION ITEM
// ==========================================

@Composable
private fun ActivitySessionItem(
    activity: PlaybackActivity,
    onPlayClick: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTimeStr = timeFormatter.format(Date(activity.startTime))
    val endTimeStr = timeFormatter.format(Date(activity.endTime))
    val durationMinutes = (activity.activeDurationMs / 60000L).coerceAtLeast(1L)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(activity.artworkUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.songTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
                Text(
                    text = "${activity.artistName} • ${activity.albumName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray93,
                    maxLines = 1
                )
                Text(
                    text = "$startTimeStr - $endTimeStr (${durationMinutes} phút)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleRed,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Play",
                    tint = AppleRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
