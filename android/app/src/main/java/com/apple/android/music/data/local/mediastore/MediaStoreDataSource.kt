package com.apple.android.music.data.local.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.util.Log
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.model.Artist
import com.apple.android.music.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.os.Environment
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for reading audio files from device storage using MediaStore API
 */
@Singleton
class MediaStoreDataSource @Inject constructor(
    private val contentResolver: ContentResolver
) {
    companion object {
        private val ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart")
        
        private val SONG_PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )
        
        private val ALBUM_PROJECTION = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )
        
        private val ARTIST_PROJECTION = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
        )
    }

    /**
     * Query all songs from MediaStore, optionally filtered by folders
     */
    suspend fun getAllSongs(allowedFolders: Set<String>? = null): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        Log.d("MusicApp", "MediaStoreDataSource: getAllSongs started")
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        // REMOVE SELECTION: Check if anything at all comes back from the query
        val selection = null 
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        
        try {
            contentResolver.query(
                collection,
                SONG_PROJECTION,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                Log.d("MusicApp", "MediaStoreDataSource: query (NO FILTER) returned ${cursor.count} results")
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val path = cursor.getString(dataColumn) ?: "No path"
                    val duration = cursor.getLong(durationColumn)
                    
                    Log.d("MusicApp", "MediaStoreDataSource: FOUND: $title | path: $path | dur: $duration")
                    
                    // Even if we founded it, we only add it if it has duration > 0
                    if (duration > 0) {
                        val albumId = cursor.getLong(albumIdColumn)
                        
                        // Filter by folder if allowedFolders is provided
                        val shouldAdd = if (allowedFolders == null || allowedFolders.isEmpty()) {
                            true
                        } else {
                            val file = File(path)
                            val parentPath = file.parent ?: ""
                            allowedFolders.any { parentPath.contains(it, ignoreCase = true) }
                        }

                        if (shouldAdd) {
                            val song = Song(
                                id = id,
                                title = title,
                                artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                                album = cursor.getString(albumColumn) ?: "Unknown Album",
                                duration = duration,
                                uri = ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                ),
                                albumId = albumId,
                                albumArtUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
                            )
                            songs.add(song)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicApp", "MediaStoreDataSource: error querying songs", e)
        }
        
        Log.d("MusicApp", "MediaStoreDataSource: returning ${songs.size} songs")
        songs
    }

    /**
     * Query all albums from MediaStore
     */
    suspend fun getAllAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }
        
        val sortOrder = "${MediaStore.Audio.Albums.ALBUM} ASC"
        
        contentResolver.query(
            collection,
            ALBUM_PROJECTION,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val songCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                
                val album = Album(
                    id = id,
                    name = cursor.getString(albumColumn) ?: "Unknown Album",
                    artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                    songCount = cursor.getInt(songCountColumn),
                    albumArtUri = ContentUris.withAppendedId(ALBUM_ART_URI, id)
                )
                albums.add(album)
            }
        }
        
        albums
    }

    /**
     * Query all artists from MediaStore
     */
    suspend fun getAllArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Artists.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        }
        
        val sortOrder = "${MediaStore.Audio.Artists.ARTIST} ASC"
        
        contentResolver.query(
            collection,
            ARTIST_PROJECTION,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val songCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            val albumCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            
            while (cursor.moveToNext()) {
                val artist = Artist(
                    id = cursor.getLong(idColumn),
                    name = cursor.getString(artistColumn) ?: "Unknown Artist",
                    songCount = cursor.getInt(songCountColumn),
                    albumCount = cursor.getInt(albumCountColumn)
                )
                artists.add(artist)
            }
        }
        
        artists
    }

    /**
     * Get songs for a specific album
     */
    suspend fun getSongsForAlbum(albumId: Long): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val selection = "${MediaStore.Audio.Media.ALBUM_ID} = ? AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selectionArgs = arrayOf(albumId.toString())
        val sortOrder = "${MediaStore.Audio.Media.TRACK} ASC"
        
        contentResolver.query(
            collection,
            SONG_PROJECTION,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val albId = cursor.getLong(albumIdCol)
                
                val song = Song(
                    id = id,
                    title = cursor.getString(titleColumn) ?: "Unknown",
                    artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                    album = cursor.getString(albumColumn) ?: "Unknown Album",
                    duration = cursor.getLong(durationColumn),
                    uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ),
                    albumId = albId,
                    albumArtUri = ContentUris.withAppendedId(ALBUM_ART_URI, albId)
                )
                songs.add(song)
            }
        }
        
        songs
    }

    /**
     * Get songs for a specific artist
     */
    suspend fun getSongsForArtist(artistName: String): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val selection = "${MediaStore.Audio.Media.ARTIST} = ? AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selectionArgs = arrayOf(artistName)
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        contentResolver.query(
            collection,
            SONG_PROJECTION,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)
                
                val song = Song(
                    id = id,
                    title = cursor.getString(titleColumn) ?: "Unknown",
                    artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                    album = cursor.getString(albumColumn) ?: "Unknown Album",
                    duration = cursor.getLong(durationColumn),
                    uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ),
                    albumId = albumId,
                    albumArtUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
                )
                songs.add(song)
            }
        }
        
        songs
    }

    /**
     * Finds all folders containing music within the standard Music directory
     */
    suspend fun getMusicFolders(): List<String> = withContext(Dispatchers.IO) {
        val folders = mutableSetOf<String>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataColumn)
                    if (path != null) {
                        val file = File(path)
                        val parent = file.parent
                        if (parent != null) {
                            folders.add(parent)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicApp", "Error getting music folders", e)
        }
        
        // Filter: Only take folders that contain "Music" or are in the Music directory
        val publicMusicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
        
        folders.filter { path ->
            path.contains(publicMusicPath, ignoreCase = true) || path.contains("/Music/", ignoreCase = true)
        }.toList().sorted()
    }
}
