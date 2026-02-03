package com.apple.android.music

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.apple.android.music.util.AudioArtworkFetcher
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Hilt dependency injection and Coil configuration
 */
@HiltAndroidApp
class MusicApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(AudioArtworkFetcher.Factory(this@MusicApp))
            }
            .build()
    }
}
