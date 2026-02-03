package com.apple.android.music.util

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import java.io.File

object MediaScannerUtil {
    fun scanMusicFolder(context: Context) {
        val root = File("/sdcard/Music")
        if (!root.exists() || !root.isDirectory) {
            Log.d("MusicApp", "MediaScannerUtil: /sdcard/Music not found")
            return
        }

        val filePaths = mutableListOf<String>()
        root.walkTopDown().forEach { file ->
            if (file.isFile && (file.extension.equals("mp3", true) || file.extension.equals("m4a", true))) {
                filePaths.add(file.absolutePath)
                // Scan individual files immediately
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { path, uri ->
                    Log.d("MusicApp", "MediaScannerUtil: Scanned $path -> $uri")
                }
            }
        }

        if (filePaths.isEmpty()) {
            Log.d("MusicApp", "MediaScannerUtil: No audio files found in Music folder")
        } else {
            Log.d("MusicApp", "MediaScannerUtil: Triggered scan for ${filePaths.size} files")
        }
    }
}
