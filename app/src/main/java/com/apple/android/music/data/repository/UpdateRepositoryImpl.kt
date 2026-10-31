package com.apple.android.music.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.apple.android.music.domain.model.AppUpdateInfo
import com.apple.android.music.domain.model.UpdateDownloadProgress
import com.apple.android.music.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UpdateRepository {

    companion object {
        private const val GITHUB_OWNER = "khuongnv"
        private const val GITHUB_REPO = "android_music_player"
        private const val API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
        private const val USER_AGENT = "AppleMusic-Android-Updater"
        private const val UPDATE_DIR = "updates"
        private const val APK_FILE_NAME = "update.apk"
        private const val PUBLIC_APK_FILE_NAME = "AppleMusic_Update.apk"
    }

    override fun getCurrentVersionName(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "1.0.0"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
            }
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    override suspend fun checkForUpdate(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext Result.failure(IllegalStateException("Chưa có bản phát hành nào trên GitHub Releases."))
            }
            if (responseCode == 403) {
                return@withContext Result.failure(IllegalStateException("Giới hạn lượt gọi API GitHub (Rate Limit). Vui lòng thử lại sau."))
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(IllegalStateException("Lỗi kết nối GitHub (HTTP $responseCode)"))
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val tagName = json.optString("tag_name", "")
            val releaseTitle = json.optString("name", tagName)
            val releaseNotes = json.optString("body", "Không có thông tin mô tả chi tiết.")
            val assetsArray = json.optJSONArray("assets")

            var downloadUrl = ""
            var assetSize = 0L

            if (assetsArray != null) {
                // Priority 1: Pick a signed APK (does not contain "unsigned")
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true) && !assetName.contains("unsigned", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        assetSize = asset.optLong("size", 0L)
                        break
                    }
                }
                // Priority 2: Fallback to any APK only if no signed APK is found
                if (downloadUrl.isEmpty()) {
                    for (i in 0 until assetsArray.length()) {
                        val asset = assetsArray.getJSONObject(i)
                        val assetName = asset.optString("name", "")
                        if (assetName.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            assetSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }
            }

            val currentVersion = getCurrentVersionName()
            val cleanRemoteVersion = tagName.removePrefix("v").removePrefix("V")
            val isAvailable = isNewerVersion(remoteTag = cleanRemoteVersion, currentVersion = currentVersion) && downloadUrl.isNotBlank()

            Result.success(
                AppUpdateInfo(
                    currentVersion = currentVersion,
                    newVersion = cleanRemoteVersion.ifBlank { currentVersion },
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    assetSize = assetSize,
                    isUpdateAvailable = isAvailable
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun downloadUpdate(downloadUrl: String): Flow<UpdateDownloadProgress> = flow {
        val updateDir = File(context.cacheDir, UPDATE_DIR).apply { if (!exists()) mkdirs() }
        val apkFile = File(updateDir, APK_FILE_NAME)
        if (apkFile.exists()) {
            apkFile.delete()
        }

        var currentUrl = downloadUrl
        var connection: HttpURLConnection? = null
        var redirectCount = 0
        val maxRedirects = 5

        // Handle redirects (GitHub 302 to AWS S3 storage)
        while (redirectCount < maxRedirects) {
            val url = URL(currentUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 20000
                readTimeout = 30000
            }

            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || 
                status == HttpURLConnection.HTTP_MOVED_PERM || 
                status == 307 || status == 308) {
                currentUrl = connection.getHeaderField("Location")
                redirectCount++
                connection.disconnect()
            } else {
                break
            }
        }

        val activeConnection = connection ?: throw IllegalStateException("Không thể khởi tạo kết nối")
        if (activeConnection.responseCode != HttpURLConnection.HTTP_OK) {
            throw IllegalStateException("Lỗi tải file cập nhật (HTTP ${activeConnection.responseCode})")
        }

        val totalBytes = activeConnection.contentLengthLong
        var bytesDownloaded = 0L
        val buffer = ByteArray(8192)

        activeConnection.inputStream.use { input: InputStream ->
            FileOutputStream(apkFile).use { output: FileOutputStream ->
                var bytesRead: Int
                var lastEmittedPercent = -1

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    bytesDownloaded += bytesRead

                    val percent = if (totalBytes > 0) {
                        ((bytesDownloaded * 100) / totalBytes).toInt()
                    } else {
                        0
                    }

                    if (percent != lastEmittedPercent) {
                        lastEmittedPercent = percent
                        emit(
                            UpdateDownloadProgress(
                                bytesDownloaded = bytesDownloaded,
                                totalBytes = totalBytes,
                                percent = percent
                            )
                        )
                    }
                }
                output.flush()
            }
        }

        // Export copy to Public Downloads directory so user retains the APK even if uninstalled
        saveApkToPublicDownloads(apkFile)

        // Final 100% emission
        emit(
            UpdateDownloadProgress(
                bytesDownloaded = bytesDownloaded,
                totalBytes = if (totalBytes > 0) totalBytes else bytesDownloaded,
                percent = 100
            )
        )
    }.flowOn(Dispatchers.IO)

    private fun saveApkToPublicDownloads(sourceFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                try {
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                    val selectionArgs = arrayOf(PUBLIC_APK_FILE_NAME, "${Environment.DIRECTORY_DOWNLOADS}%")
                    resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, selection, selectionArgs)
                } catch (ignored: Exception) {}

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, PUBLIC_APK_FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                        out.flush()
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    Log.d("UpdateRepo", "Saved APK to MediaStore.Downloads: $PUBLIC_APK_FILE_NAME")
                }
            }

            // Also copy directly to public Environment.DIRECTORY_DOWNLOADS file if accessible
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (publicDir.exists() || publicDir.mkdirs()) {
                val targetFile = File(publicDir, PUBLIC_APK_FILE_NAME)
                sourceFile.copyTo(targetFile, overwrite = true)
                targetFile.setReadable(true, false)
                Log.d("UpdateRepo", "Saved APK to direct Downloads: ${targetFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("UpdateRepo", "Failed to save APK to public downloads folder", e)
        }
    }

    override fun getDownloadedApkFile(): File? {
        val cacheApk = File(File(context.cacheDir, UPDATE_DIR), APK_FILE_NAME)
        if (cacheApk.exists() && cacheApk.length() > 0) return cacheApk

        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val publicApk = File(publicDir, PUBLIC_APK_FILE_NAME)
        if (publicApk.exists() && publicApk.length() > 0) return publicApk

        return null
    }

    override fun deleteDownloadedApk() {
        val cacheApk = File(File(context.cacheDir, UPDATE_DIR), APK_FILE_NAME)
        if (cacheApk.exists()) {
            cacheApk.delete()
        }
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val publicApk = File(publicDir, PUBLIC_APK_FILE_NAME)
        if (publicApk.exists()) {
            publicApk.delete()
        }
    }

    private fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
        val remoteNumbers = remoteTag.removePrefix("v").removePrefix("V")
            .split(".")
            .mapNotNull { it.toIntOrNull() }
        val localNumbers = currentVersion.removePrefix("v").removePrefix("V")
            .split(".")
            .mapNotNull { it.toIntOrNull() }

        if (remoteNumbers.isEmpty() || localNumbers.isEmpty()) return false

        val maxLen = maxOf(remoteNumbers.size, localNumbers.size)
        for (i in 0 until maxLen) {
            val r = remoteNumbers.getOrElse(i) { 0 }
            val l = localNumbers.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
