package com.music.player.domain.repository

import com.music.player.domain.model.Playlist
import com.music.player.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for playlist operations
 */
interface PlaylistRepository {
    
    fun getAllPlaylists(): Flow<List<Playlist>>
    
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    
    suspend fun createPlaylist(name: String): Long
    
    suspend fun updatePlaylist(playlist: Playlist)
    
    suspend fun deletePlaylist(playlistId: Long)
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>)
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    
    suspend fun clearPlaylist(playlistId: Long)
    
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
    
    suspend fun reorderSongs(playlistId: Long, songId: Long, newPosition: Int)
}
