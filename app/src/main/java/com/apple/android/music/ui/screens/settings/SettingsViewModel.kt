package com.apple.android.music.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apple.android.music.data.local.updater.ApkInstaller
import com.apple.android.music.domain.model.UpdateStatus
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.SettingsRepository
import com.apple.android.music.domain.usecase.CheckAppUpdateUseCase
import com.apple.android.music.domain.usecase.DownloadAppUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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

    init {
        loadFolders()
        observeSleepTimerSettings()
        // Check if an APK was already downloaded in previous session
        val cachedApk = downloadAppUpdateUseCase.getDownloadedApkFile()
        if (cachedApk != null) {
            _uiState.update { it.copy(updateStatus = UpdateStatus.ReadyToInstall(cachedApk)) }
        }
    }

    private fun observeSleepTimerSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.sleepTimerEnabled,
                settingsRepository.sleepTimerDuration
            ) { enabled, duration ->
                Triple(enabled, duration, System.currentTimeMillis())
            }.collect { (enabled, duration, currentTime) ->
                val endTimeString = if (enabled && duration > 0) {
                    val endTimeMillis = currentTime + (duration * 60 * 1000L)
                    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    formatter.format(java.util.Date(endTimeMillis))
                } else {
                    null
                }

                _uiState.update {
                    it.copy(
                        sleepTimerEnabled = enabled,
                        sleepTimerDuration = duration.toString(),
                        sleepTimerEndTime = endTimeString
                    )
                }
            }
        }
    }

    fun loadFolders() {
        viewModelScope.launch {
            settingsRepository.selectedFolders.collect { selected ->
                val foldersInfo = selected.map { path ->
                    FolderInfo(
                        path = path,
                        name = try { File(path).name.ifEmpty { path } } catch (e: Exception) { path },
                        isSelected = true
                    )
                }
                _uiState.update { it.copy(folders = foldersInfo) }
            }
        }
    }

    fun setSleepTimerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSleepTimerEnabled(enabled)
        }
    }

    fun setSleepTimerDuration(durationStr: String) {
        if (durationStr.all { it.isDigit() }) {
            _uiState.update { it.copy(sleepTimerDuration = durationStr) }
            val duration = durationStr.toIntOrNull()
            if (duration != null && duration > 0) {
                viewModelScope.launch {
                    settingsRepository.setSleepTimerDuration(duration)
                }
            }
        }
    }

    fun addFolder(uri: android.net.Uri) {
        viewModelScope.launch {
            val currentSelected = settingsRepository.getSelectedFolders().toMutableSet()
            currentSelected.add(uri.toString())
            settingsRepository.setSelectedFolders(currentSelected)
            refreshLibrary()
        }
    }

    fun removeFolder(path: String) {
        viewModelScope.launch {
            val currentSelected = settingsRepository.getSelectedFolders().toMutableSet()
            currentSelected.remove(path)
            settingsRepository.setSelectedFolders(currentSelected)
            refreshLibrary()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            musicRepository.refreshMusic()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _uiState.update { it.copy(isLoading = false) }
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
                _uiState.update {
                    it.copy(updateStatus = UpdateStatus.Error(error.message ?: "Không thể mở file cài đặt."))
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }
}
