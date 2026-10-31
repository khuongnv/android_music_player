package com.apple.android.music.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.data.local.updater.ApkInstaller
import com.apple.android.music.domain.model.ThemeMode
import com.apple.android.music.domain.model.UpdateStatus
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.SettingsRepository
import com.apple.android.music.domain.usecase.CheckAppUpdateUseCase
import com.apple.android.music.domain.usecase.DownloadAppUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    private val checkAppUpdateUseCase: CheckAppUpdateUseCase,
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            currentVersion = checkAppUpdateUseCase.getCurrentVersion()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var timerTickerJob: Job? = null

    init {
        observeThemeMode()
        loadFolders()
        observeSleepTimer()
        loadCacheSize()
        checkCachedApk()
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    private fun checkCachedApk() {
        val cachedApk = downloadAppUpdateUseCase.getDownloadedApkFile()
        if (cachedApk != null) {
            _uiState.update { it.copy(updateStatus = UpdateStatus.ReadyToInstall(cachedApk)) }
        }
    }

    // --- Sleep Timer ---

    private fun observeSleepTimer() {
        viewModelScope.launch {
            combine(
                settingsRepository.sleepTimerEnabled,
                settingsRepository.sleepTimerDuration,
                settingsRepository.sleepTimerTargetTime
            ) { enabled, duration, targetTime ->
                Triple(enabled, duration, targetTime)
            }.collect { (enabled, duration, targetTime) ->
                val now = System.currentTimeMillis()
                val remainingSeconds = if (enabled && targetTime > now) {
                    (targetTime - now) / 1000L
                } else {
                    0L
                }

                val endTimeString = if (enabled && targetTime > now) {
                    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    formatter.format(Date(targetTime))
                } else {
                    null
                }

                _uiState.update {
                    it.copy(
                        sleepTimerEnabled = enabled,
                        sleepTimerDurationMinutes = duration,
                        sleepTimerTargetTime = targetTime,
                        sleepTimerRemainingSeconds = remainingSeconds,
                        sleepTimerEndTimeString = endTimeString
                    )
                }

                if (enabled && targetTime > now) {
                    startTimerTicker(targetTime)
                } else {
                    timerTickerJob?.cancel()
                }
            }
        }
    }

    private fun startTimerTicker(targetTime: Long) {
        timerTickerJob?.cancel()
        timerTickerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val diffSeconds = (targetTime - now) / 1000L
                if (diffSeconds <= 0L) {
                    _uiState.update {
                        it.copy(
                            sleepTimerEnabled = false,
                            sleepTimerRemainingSeconds = 0L,
                            sleepTimerEndTimeString = null
                        )
                    }
                    break
                }
                _uiState.update { it.copy(sleepTimerRemainingSeconds = diffSeconds) }
                delay(1000L)
            }
        }
    }

    fun setSleepTimerPreset(minutes: Int) {
        viewModelScope.launch {
            if (minutes > 0) {
                settingsRepository.setSleepTimer(minutes)
                _uiState.update {
                    it.copy(toastMessage = "Đã hẹn giờ tắt nhạc sau $minutes phút.")
                }
            } else {
                cancelSleepTimer()
            }
        }
    }

    fun cancelSleepTimer() {
        viewModelScope.launch {
            settingsRepository.cancelSleepTimer()
            _uiState.update {
                it.copy(
                    sleepTimerEnabled = false,
                    sleepTimerRemainingSeconds = 0L,
                    sleepTimerEndTimeString = null,
                    toastMessage = "Đã tắt hẹn giờ nghe nhạc."
                )
            }
        }
    }

    // --- Folders & Library ---

    fun loadFolders() {
        viewModelScope.launch {
            settingsRepository.selectedFolders.collect { selected ->
                val foldersInfo = selected.map { uriString ->
                    FolderInfo(
                        uriString = uriString,
                        name = formatFolderDisplayName(uriString),
                        path = uriString,
                        isSelected = true
                    )
                }
                _uiState.update { it.copy(folders = foldersInfo) }
            }
        }
    }

    fun addFolder(uri: Uri) {
        viewModelScope.launch {
            val currentSelected = settingsRepository.getSelectedFolders().toMutableSet()
            currentSelected.add(uri.toString())
            settingsRepository.setSelectedFolders(currentSelected)
            _uiState.update {
                it.copy(toastMessage = "Đã thêm thư mục quét nhạc.")
            }
            refreshLibrary()
        }
    }

    fun removeFolder(uriString: String) {
        viewModelScope.launch {
            val currentSelected = settingsRepository.getSelectedFolders().toMutableSet()
            currentSelected.remove(uriString)
            settingsRepository.setSelectedFolders(currentSelected)
            _uiState.update {
                it.copy(toastMessage = "Đã xóa thư mục.")
            }
            refreshLibrary()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            try {
                musicRepository.refreshMusic()
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        toastMessage = "Đã làm mới thư viện nhạc thành công."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        toastMessage = "Lỗi khi quét thư viện: ${e.message}"
                    )
                }
            }
        }
    }

    // --- Cache & Storage ---

    fun loadCacheSize() {
        viewModelScope.launch {
            val bytes = settingsRepository.getCacheSizeBytes()
            _uiState.update {
                it.copy(
                    cacheSizeBytes = bytes,
                    cacheSizeFormatted = formatFileSize(bytes)
                )
            }
        }
    }

    fun showClearCacheConfirmation(show: Boolean) {
        _uiState.update { it.copy(showClearCacheConfirm = show) }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isClearingCache = true,
                    showClearCacheConfirm = false
                )
            }
            val success = settingsRepository.clearCache()
            loadCacheSize()
            _uiState.update {
                it.copy(
                    isClearingCache = false,
                    toastMessage = if (success) "Đã xóa bộ nhớ đệm thành công." else "Không thể xóa hoàn toàn bộ nhớ đệm."
                )
            }
        }
    }

    // --- Audio & Equalizer ---

    fun openEqualizer(context: Context) {
        try {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            _uiState.update {
                it.copy(toastMessage = "Thiết bị không có ứng dụng Bộ cân bằng âm thanh (Equalizer) tương thích.")
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(toastMessage = "Không thể mở bộ chỉnh âm: ${e.message}")
            }
        }
    }

    // --- Auto-Update Functions ---

    fun checkForUpdates(isManual: Boolean = true) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updateStatus = UpdateStatus.Checking,
                    showUpdateDialog = isManual
                )
            }

            val result = checkAppUpdateUseCase()
            result.onSuccess { info ->
                if (info.isUpdateAvailable) {
                    _uiState.update {
                        it.copy(
                            updateStatus = UpdateStatus.UpdateAvailable(info),
                            showUpdateDialog = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            updateStatus = UpdateStatus.UpToDate(info.currentVersion),
                            showUpdateDialog = isManual
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        updateStatus = UpdateStatus.Error(error.message ?: "Không thể kiểm tra bản cập nhật."),
                        showUpdateDialog = isManual
                    )
                }
            }
        }
    }

    fun startDownloadUpdate() {
        val currentStatus = _uiState.value.updateStatus
        if (currentStatus !is UpdateStatus.UpdateAvailable) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updateStatus = UpdateStatus.Downloading(
                        progressPercent = 0,
                        bytesDownloaded = 0L,
                        totalBytes = currentStatus.updateInfo.assetSize
                    )
                )
            }

            downloadAppUpdateUseCase(currentStatus.updateInfo.downloadUrl)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            updateStatus = UpdateStatus.Error(error.message ?: "Lỗi tải bản cập nhật.")
                        )
                    }
                }
                .collect { progress ->
                    _uiState.update {
                        it.copy(
                            updateStatus = UpdateStatus.Downloading(
                                progressPercent = progress.percent,
                                bytesDownloaded = progress.bytesDownloaded,
                                totalBytes = progress.totalBytes
                            )
                        )
                    }
                    if (progress.percent >= 100) {
                        val apkFile = downloadAppUpdateUseCase.getDownloadedApkFile()
                        if (apkFile != null) {
                            _uiState.update {
                                it.copy(updateStatus = UpdateStatus.ReadyToInstall(apkFile))
                            }
                        }
                    }
                }
        }
    }

    fun installUpdate(context: Context) {
        val status = _uiState.value.updateStatus
        if (status is UpdateStatus.ReadyToInstall) {
            val result = ApkInstaller.installApk(context, status.apkFile)
            result.onFailure { error ->
                if (error is SecurityException) {
                    _uiState.update {
                        it.copy(toastMessage = "Vui lòng cấp quyền cài đặt từ nguồn này rồi bấm Cài đặt lại.")
                    }
                } else {
                    _uiState.update {
                        it.copy(updateStatus = UpdateStatus.Error(error.message ?: "Không thể mở file cài đặt."))
                    }
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // --- Helpers ---

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 0.1 -> String.format(Locale.US, "%.1f MB", mb)
            else -> String.format(Locale.US, "%.0f KB", kb)
        }
    }

    private fun formatFolderDisplayName(uriString: String): String {
        return try {
            val uri = Uri.parse(uriString)
            val decoded = Uri.decode(uri.lastPathSegment ?: uriString)
            when {
                decoded.contains(":") -> {
                    val parts = decoded.split(":")
                    val folder = parts.getOrNull(1)?.ifEmpty { "Bộ nhớ trong" } ?: "Bộ nhớ trong"
                    if (folder == "Bộ nhớ trong") "Thư mục gốc thiết bị" else "📁 $folder"
                }
                else -> "📁 $decoded"
            }
        } catch (e: Exception) {
            uriString
        }
    }
}
