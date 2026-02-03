package com.apple.android.music.data.local.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.apple.android.music.data.local.room.dao.AlbumDao
import com.apple.android.music.data.local.room.dao.ArtistDao
import com.apple.android.music.data.local.room.dao.SongDao
import com.apple.android.music.data.local.room.entity.AlbumEntity
import com.apple.android.music.data.local.room.entity.ArtistEntity
import com.apple.android.music.data.local.room.entity.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

import dagger.hilt.android.qualifiers.ApplicationContext

class MusicScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao
) {

    private val supportedExtensions = setOf("mp3", "flac", "wav", "m4a", "aac")

    suspend fun scanDirectory(treeUri: Uri) = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext
        scanRecursive(rootDoc)
    }

    private suspend fun scanRecursive(directory: DocumentFile) {
        val files = directory.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                scanRecursive(file)
            } else if (file.isFile) {
                val extension = file.name?.substringAfterLast('.', "")?.lowercase()
                if (extension in supportedExtensions) {
                    processFile(file)
                }
            }
        }
    }

    private suspend fun processFile(file: DocumentFile) {
        // Optimization: Check if file already exists in DB by path (URI string)
        val existingSong = songDao.getSongByPath(file.uri.toString())
        if (existingSong != null) return

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, file.uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.name ?: "Unknown Title"
            val artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            val yearStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            val year = yearStr?.toIntOrNull()

            // Handle Artist
            var artist = artistDao.getArtistByName(artistName)
            val artistId: Long
            if (artist == null) {
                artistId = artistDao.insertArtist(ArtistEntity(name = artistName))
                // Reload to capture ID if needed, or use return value
                // insertArtist returns Long ID
            } else {
                artistId = artist.id
            }

            // Handle Album
            var album = albumDao.getAlbumByNameAndArtist(albumName, artistName)
            val albumId: Long
            if (album == null) {
                // Try to get embedded picture
                // Note: Reading embedded picture is heavy. We might want to store URI to file and load with Coil using audio URI.
                // Or save to internal storage.
                // For now, let's just use the file URI as artwork source for Coil (it supports audio file artwork).
                albumId = albumDao.insertAlbum(
                    AlbumEntity(
                        title = albumName, 
                        artist = artistName,
                        year = year,
                        artworkPath = file.uri.toString() // Use first song's URI as album art source
                    )
                )
            } else {
                albumId = album.id
            }

            // Insert Song
            val song = SongEntity(
                title = title,
                artist = artistName,
                album = albumName,
                path = file.uri.toString(),
                duration = duration,
                size = file.length(),
                dateAdded = System.currentTimeMillis(),
                artistId = artistId,
                albumId = albumId,
                artworkUri = file.uri.toString()
            )
            songDao.insertSong(song)

        } catch (e: Exception) {
            Log.e("MusicScanner", "Error scanning file ${file.name}", e)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        songDao.clearAll()
        albumDao.clearAll()
        artistDao.clearAll()
        // Playlists? Maybe keep them but clean up invalid songs? 
        // For now, full clear as requested "Clear Library Cache".
    }
}
