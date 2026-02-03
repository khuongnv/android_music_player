package com.apple.android.music.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import android.graphics.BitmapFactory

@OptIn(UnstableApi::class)
class CoilBitmapLoader(private val context: Context) : BitmapLoader {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun loadBitmap(uri: Uri, options: BitmapFactory.Options?): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()

        scope.launch {
            try {
                // Use singleton ImageLoader which has the AudioArtworkFetcher registered
                val result = coil.Coil.imageLoader(context).execute(
                    ImageRequest.Builder(context)
                        .data(uri)
                        .allowHardware(false) // Important for remote views/notifications
                        .size(300, 300) // Notification icons are small, 300x300 is plenty
                        .scale(Scale.FILL)
                        .build()
                )

                val drawable = result.drawable
                if (drawable is BitmapDrawable) {
                    future.set(drawable.bitmap)
                } else {
                    // Try to draw it to a bitmap if it's not one (e.g. vector)
                    // Or just fail gracefully
                     future.setException(IllegalArgumentException("Could not convert drawable to bitmap"))
                }
            } catch (e: Exception) {
                future.setException(e)
            }
        }
        return future
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        scope.launch {
            try {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                if (bitmap != null) {
                    future.set(bitmap)
                } else {
                    future.setException(IllegalArgumentException("Could not decode bitmap from byte array"))
                }
            } catch (e: Exception) {
                future.setException(e)
            }
        }
        return future
    }
}
