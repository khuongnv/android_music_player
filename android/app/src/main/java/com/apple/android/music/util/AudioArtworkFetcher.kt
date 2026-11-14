package com.apple.android.music.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream

/**
 * A Coil Fetcher that extracts embedded artwork from audio files (mp3, m4a, etc.)
 */
class AudioArtworkFetcher(
    private val context: Context,
    private val data: Uri,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, data)
            val picture = retriever.embeddedPicture
            if (picture != null) {
                SourceResult(
                    source = ImageSource(
                        source = Buffer().apply { write(picture) },
                        context = context
                    ),
                    mimeType = null,
                    dataSource = DataSource.DISK
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            // Check if the URI is a local file or content URI and looks like an audio file
            // Alternatively, we can let it try and fail if it's not supported.
            // For our app, we know albumArtUri points to the audio file.
            val scheme = data.scheme
            if (scheme == "content" || scheme == "file") {
                val path = data.path?.lowercase() ?: ""
                if (path.endsWith(".mp3") || path.endsWith(".m4a") || path.endsWith(".flac") || 
                    path.endsWith(".wav") || path.endsWith(".aac")) {
                    return AudioArtworkFetcher(context, data, options)
                }
                
                // Content URIs might not have extension, we can also check mime type via context
                val mimeType = context.contentResolver.getType(data)
                if (mimeType?.startsWith("audio/") == true) {
                    return AudioArtworkFetcher(context, data, options)
                }
            }
            return null
        }
    }
}
