package com.apple.android.music.data.local.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.apple.android.music.data.local.room.dao.AlbumDao
import com.apple.android.music.data.local.room.dao.ArtistDao
import com.apple.android.music.data.local.room.dao.SongDao
import com.apple.android.music.data.local.room.entity.AlbumEntity
import com.apple.android.music.data.local.room.entity.ArtistEntity
import com.apple.android.music.data.local.room.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
            val artist = artistDao.getArtistByName(artistName)
            val artistId: Long = if (artist == null) {
                artistDao.insertArtist(ArtistEntity(name = artistName))
            } else {
                artist.id
            }

            // Handle Album
            val album = albumDao.getAlbumByNameAndArtist(albumName, artistName)
            val albumId: Long = if (album == null) {
                albumDao.insertAlbum(
                    AlbumEntity(
                        title = albumName, 
                        artist = artistName,
                        year = year,
                        artworkPath = file.uri.toString() // Use first song's URI as album art source
                    )
                )
            } else {
                album.id
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

    suspend fun scanMediaStore() = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val uriString = uri.toString()
                    val existing = songDao.getSongByPath(uriString)
                    if (existing != null) continue

                    val duration = cursor.getLong(durCol)
                    if (duration <= 0) continue

                    val title = cursor.getString(titleCol) ?: "Unknown Title"
                    val artistName = cursor.getString(artistCol) ?: "Unknown Artist"
                    val albumName = cursor.getString(albumCol) ?: "Unknown Album"
                    val mediaStoreAlbumId = cursor.getLong(albumIdCol)
                    val size = cursor.getLong(sizeCol)
                    val dateAdded = cursor.getLong(dateCol)

                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        mediaStoreAlbumId
                    ).toString()

                    val artist = artistDao.getArtistByName(artistName)
                    val artistId = if (artist == null) {
                        artistDao.insertArtist(ArtistEntity(name = artistName))
                    } else {
                        artist.id
                    }

                    val album = albumDao.getAlbumByNameAndArtist(albumName, artistName)
                    val albumId = if (album == null) {
                        albumDao.insertAlbum(
                            AlbumEntity(
                                title = albumName,
                                artist = artistName,
                                year = null,
                                artworkPath = albumArtUri
                            )
                        )
                    } else {
                        album.id
                    }

                    val song = SongEntity(
                        title = title,
                        artist = artistName,
                        album = albumName,
                        path = uriString,
                        duration = duration,
                        size = size,
                        dateAdded = dateAdded,
                        artistId = artistId,
                        albumId = albumId,
                        artworkUri = albumArtUri
                    )
                    songDao.insertSong(song)
                }
            }
        } catch (e: Exception) {
            Log.e("MusicScanner", "Error scanning MediaStore", e)
        }
    }
    
    suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        songDao.clearAll()
        albumDao.clearAll()
        artistDao.clearAll()
    }
}
