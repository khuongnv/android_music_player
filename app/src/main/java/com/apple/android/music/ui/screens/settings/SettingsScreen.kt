package com.apple.android.music.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.apple.android.music.domain.model.ThemeMode
import com.apple.android.music.domain.model.UpdateStatus
import com.apple.android.music.ui.theme.AppleRed
import com.apple.android.music.ui.theme.Gray93
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Custom duration dialog state
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var customDurationInput by remember { mutableStateOf("45") }

    // Show feedback messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearToastMessage()
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: Exception) {
                // Ignore security exception if already granted
            }
            viewModel.addFolder(it)
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
            // Large Title - Apple Music style
            item {
                Text(
                    text = "Cài đặt",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp
                    ),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
                )
            }

            // --- SECTION: GIAO DIỆN & CHỦ ĐỀ (THEME) ---
            item {
                SettingsSectionHeader(title = "GIAO DIỆN & CHỦ ĐỀ")
                SettingsGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF007AFF)), // iOS Blue
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chế độ giao diện",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (uiState.themeMode) {
                                        ThemeMode.SYSTEM -> "Theo hệ thống"
                                        ThemeMode.LIGHT -> "Giao diện Sáng (Light)"
                                        ThemeMode.DARK -> "Giao diện Tối (AMOLED Dark)"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = Gray93,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Apple-style 3-Segment Control
                        ThemeSegmentedPicker(
                            selectedMode = uiState.themeMode,
                            onSelectMode = { viewModel.setThemeMode(it) }
                        )
                    }
                }
            }

            // --- SECTION 1: CẬP NHẬT PHẦN MỀM ---
            item {
                SettingsSectionHeader(title = "BẢN CẬP NHẬT PHẦN MỀM")
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Default.SystemUpdate,
                        iconBackground = Color(0xFFFF9500), // iOS Orange
                        title = "Cập nhật ứng dụng",
                        subtitle = "Phiên bản hiện tại: v${uiState.currentVersion}",
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppleRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "v${uiState.currentVersion}",
                                        color = AppleRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Gray93.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        },
                        onClick = { viewModel.checkForUpdates(isManual = true) }
                    )
                }
            }

            // --- SECTION 2: HẸN GIỜ TẮT NHẠC (SLEEP TIMER) ---
            item {
                SettingsSectionHeader(title = "HẸN GIỜ TẮT NHẠC")
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Default.Timer,
                        iconBackground = Color(0xFF5856D6), // iOS Purple
                        title = "Hẹn giờ ngủ",
                        subtitle = if (uiState.sleepTimerEnabled) {
                            val mins = uiState.sleepTimerRemainingSeconds / 60
                            val secs = uiState.sleepTimerRemainingSeconds % 60
                            val countdown = String.format(Locale.US, "%02d:%02d", mins, secs)
                            "Còn lại $countdown (Tắt lúc ${uiState.sleepTimerEndTimeString ?: ""})"
                        } else {
                            "Đang tắt"
                        },
                        trailing = {
                            Switch(
                                checked = uiState.sleepTimerEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        viewModel.setSleepTimerPreset(uiState.sleepTimerDurationMinutes)
                                    } else {
                                        viewModel.cancelSleepTimer()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppleRed,
                                    uncheckedThumbColor = Gray93,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    )

                    // Presets container
                    AnimatedVisibility(
                        visible = uiState.sleepTimerEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Text(
                                text = "Chọn thời gian:",
                                style = MaterialTheme.typography.labelMedium,
                                color = Gray93,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Preset chips row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = listOf(15, 30, 45, 60)
                                presets.forEach { minutes ->
                                    val isSelected = uiState.sleepTimerDurationMinutes == minutes
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setSleepTimerPreset(minutes) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) AppleRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${minutes}p",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }

                                // Custom option chip
                                val isCustom = uiState.sleepTimerDurationMinutes !in listOf(15, 30, 45, 60)
                                Surface(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clickable { showCustomDurationDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCustom) AppleRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isCustom) "${uiState.sleepTimerDurationMinutes}p" else "Tùy chọn",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCustom) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Cancel timer action
                            TextButton(
                                onClick = { viewModel.cancelSleepTimer() },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Dừng hẹn giờ ngay",
                                    color = AppleRed,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 3: ÂM THANH & HIỆU ỨNG (AUDIO & EQUALIZER) ---
            item {
                SettingsSectionHeader(title = "ÂM THANH & HIỆU ỨNG")
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Default.GraphicEq,
                        iconBackground = AppleRed,
                        title = "Bộ cân bằng âm thanh (Equalizer)",
                        subtitle = "Mở bảng điều khiển âm thanh hệ thống",
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Gray93.copy(alpha = 0.6f),
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        onClick = { viewModel.openEqualizer(context) }
                    )
                }
            }

            // --- SECTION 4: THƯ VIỆN NHẠC (MUSIC LIBRARY) ---
            item {
                SettingsSectionHeader(title = "THƯ VIỆN & QUÉT NHẠC")
                SettingsGroupCard {
                    // Add Folder Row
                    SettingsRow(
                        icon = Icons.Default.CreateNewFolder,
                        iconBackground = Color(0xFF007AFF), // iOS Blue
                        title = "Thêm thư mục quét nhạc",
                        subtitle = "Chỉ quét tệp âm thanh trong thư mục được chọn",
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = AppleRed,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { folderPicker.launch(null) }
                    )

                    // Folders List inside Group
                    if (uiState.folders.isNotEmpty()) {
                        uiState.folders.forEach { folder ->
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.padding(start = 64.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFF007AFF),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 4.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = folder.path,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray93,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.removeFolder(folder.uriString) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove",
                                        tint = AppleRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier.padding(start = 64.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mặc định: Tự động quét toàn bộ bài hát trên thiết bị.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray93
                            )
                        }
                    }

                    // Rescan Row
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(start = 64.dp)
                    )
                    SettingsRow(
                        icon = Icons.Default.Sync,
                        iconBackground = Color(0xFF30B0C7), // iOS Teal
                        title = "Quét lại thư viện nhạc",
                        subtitle = "Đồng bộ bài hát, nghệ sĩ và album mới",
                        trailing = {
                            if (uiState.isScanning) {
                                CircularProgressIndicator(
                                    color = AppleRed,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Gray93.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        },
                        onClick = {
                            if (!uiState.isScanning) {
                                viewModel.refreshLibrary()
                            }
                        }
                    )
                }
            }

            // --- SECTION 5: BỘ NHỚ & DỮ LIỆU TẠM (STORAGE & CACHE) ---
            item {
                SettingsSectionHeader(title = "BỘ NHỚ & DỮ LIỆU TẠM")
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Default.CleaningServices,
                        iconBackground = Color(0xFF34C759), // iOS Green
                        title = "Dọn dẹp bộ nhớ đệm",
                        subtitle = "Ảnh bìa album, tệp tạm thời",
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.isClearingCache) {
                                    CircularProgressIndicator(
                                        color = AppleRed,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = uiState.cacheSizeFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Gray93
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Gray93.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        },
                        onClick = { viewModel.showClearCacheConfirmation(true) }
                    )
                }
            }

            // --- SECTION 6: THÔNG TIN ỨNG DỤNG (ABOUT) ---
            item {
                SettingsSectionHeader(title = "THÔNG TIN ỨNG DỤNG")
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Default.Code,
                        iconBackground = Color(0xFF3A3A3C),
                        title = "Mã nguồn trên GitHub",
                        subtitle = "khuongnv/android_music_player",
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Gray93.copy(alpha = 0.6f),
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        onClick = {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/khuongnv/android_music_player")
                            )
                            context.startActivity(browserIntent)
                        }
                    )
                }
            }

            // Footer
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Thiết kế theo chuẩn giao diện Apple Music",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray93.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Android Offline Music Player • 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray93.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }

    // --- DIALOG: Confirm Clear Cache ---
    if (uiState.showClearCacheConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearCacheConfirmation(false) },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Dọn dẹp bộ nhớ đệm?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Dung lượng bộ nhớ tạm hiện tại là ${uiState.cacheSizeFormatted}. Thao tác này sẽ giải phóng bộ nhớ ảnh bìa cache và tệp tải tạm thời mà không ảnh hưởng tới bài hát hay playlist của bạn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearCache() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Dọn dẹp ngay", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showClearCacheConfirmation(false) }
                ) {
                    Text("Hủy", color = Gray93)
                }
            }
        )
    }

    // --- DIALOG: Custom Sleep Timer Duration ---
    if (showCustomDurationDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDurationDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Tùy chỉnh thời gian hẹn giờ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "Nhập số phút bạn muốn hẹn giờ tắt nhạc:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray93,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = customDurationInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                customDurationInput = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val minutes = customDurationInput.toIntOrNull() ?: 30
                                if (minutes > 0) {
                                    viewModel.setSleepTimerPreset(minutes)
                                    showCustomDurationDialog = false
                                }
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minutes = customDurationInput.toIntOrNull() ?: 30
                        if (minutes > 0) {
                            viewModel.setSleepTimerPreset(minutes)
                            showCustomDurationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Bắt đầu hẹn giờ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDurationDialog = false }) {
                    Text("Hủy", color = Gray93)
                }
            }
        )
    }

    // --- DIALOG: Apple Music Software Update ---
    if (uiState.showUpdateDialog) {
        UpdateModalDialog(
            status = uiState.updateStatus,
            currentVersion = uiState.currentVersion,
            onDismiss = { viewModel.dismissUpdateDialog() },
            onDownloadClick = { viewModel.startDownloadUpdate() },
            onInstallClick = { viewModel.installUpdate(context) }
        )
    }
}

// ==========================================
// REUSABLE APPLE MUSIC SETTINGS COMPONENTS
// ==========================================

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            fontSize = 12.sp
        ),
        color = Gray93,
        modifier = Modifier.padding(start = 32.dp, top = 22.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // iOS Style Rounded Square Icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Gray93,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Trailing Content
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

// ==========================================
// APPLE MUSIC UPDATE MODAL DIALOG
// ==========================================

@Composable
private fun UpdateModalDialog(
    status: UpdateStatus,
    currentVersion: String,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(
        onDismissRequest = {
            if (status !is UpdateStatus.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = status !is UpdateStatus.Downloading,
            dismissOnClickOutside = status !is UpdateStatus.Downloading
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (status) {
                    is UpdateStatus.Checking -> {
                        CircularProgressIndicator(color = AppleRed, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang kiểm tra bản cập nhật...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kết nối tới GitHub Releases",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray93
                        )
                    }

                    is UpdateStatus.UpdateAvailable -> {
                        val info = status.updateInfo
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AppleRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = AppleRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Có bản cập nhật mới!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Phiên bản v${info.newVersion} đã sẵn sàng tải về.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray93
                        )

                        if (info.assetSize > 0) {
                            val sizeMb = String.format(Locale.US, "%.1f MB", info.assetSize / (1024.0 * 1024.0))
                            Text(
                                text = "Dung lượng: $sizeMb",
                                style = MaterialTheme.typography.labelMedium,
                                color = Gray93,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Release notes box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Nội dung cập nhật:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = info.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onDownloadClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                        ) {
                            Text("Tải và cài đặt", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Để sau", color = Gray93)
                        }
                    }

                    is UpdateStatus.Downloading -> {
                        Text(
                            text = "Đang tải bản cập nhật",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { status.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AppleRed,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val downloadedMb = String.format(Locale.US, "%.1f", status.bytesDownloaded / (1024.0 * 1024.0))
                        val totalMb = if (status.totalBytes > 0) {
                            String.format(Locale.US, "%.1f MB", status.totalBytes / (1024.0 * 1024.0))
                        } else {
                            "..."
                        }

                        Text(
                            text = "${status.progressPercent}% ($downloadedMb / $totalMb)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    is UpdateStatus.ReadyToInstall -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AppleRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppleRed,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Đã tải xong!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bản cập nhật đã được lưu an toàn tại thư mục Tải về:\n📁 Download/AppleMusic_Update.apk\n\nBạn có thể bấm Cài đặt ngay. Nếu cần gỡ bản cũ trước, tệp APK vẫn lưu sẵn trong thư mục Download để bạn cài đặt bất cứ lúc nào.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray93,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onInstallClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                        ) {
                            Text("Cài đặt ngay", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    val dlIntent = android.content.Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(dlIntent)
                                } catch (e: Exception) {
                                    try {
                                        val fileIntent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
                                            type = "*/*"
                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(fileIntent)
                                    } catch (ignored: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Mở thư mục Download", color = AppleRed, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Đóng", color = Gray93)
                        }
                    }

                    is UpdateStatus.UpToDate -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34C759).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34C759),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Ứng dụng đã là bản mới nhất",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Android Music Player v$currentVersion hiện là phiên bản mới nhất.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray93
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                        ) {
                            Text("Đồng ý", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    is UpdateStatus.Error -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Không thể kiểm tra cập nhật",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray93
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                        ) {
                            Text("Đóng", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun ThemeSegmentedPicker(
    selectedMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit
) {
    val options = listOf(
        Triple(ThemeMode.SYSTEM, "Hệ thống", Icons.Default.BrightnessAuto),
        Triple(ThemeMode.LIGHT, "Sáng", Icons.Default.LightMode),
        Triple(ThemeMode.DARK, "Tối", Icons.Default.DarkMode)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (mode, label, icon) ->
                val isSelected = selectedMode == mode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectMode(mode) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) AppleRed else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
