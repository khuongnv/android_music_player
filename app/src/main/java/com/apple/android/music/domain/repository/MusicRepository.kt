package com.apple.android.music.domain.repository

import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.model.Artist
import com.apple.android.music.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for music data operations
 */
interface MusicRepository {
    
    // Songs
    suspend fun getAllSongs(): List<Song>
    fun getSongsFlow(): Flow<List<Song>>
    suspend fun getSongById(songId: Long): Song?
    suspend fun getSongsForAlbum(albumId: Long): List<Song>
    suspend fun getSongsForArtist(artistName: String): List<Song>
    
    // Albums
    suspend fun getAllAlbums(): List<Album>
    fun getAlbumsFlow(): Flow<List<Album>>
    suspend fun getAlbumById(albumId: Long): Album?
    
    // Artists
    suspend fun getAllArtists(): List<Artist>
    fun getArtistsFlow(): Flow<List<Artist>>
    
    // Refresh data from MediaStore
    suspend fun refreshMusic()

    // Folders
    suspend fun getMusicFolders(): List<String>
}
