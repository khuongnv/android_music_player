package com.apple.android.music.data.repository

import com.apple.android.music.data.local.mediastore.MediaStoreDataSource
import com.apple.android.music.data.local.room.dao.PlaylistDao
import com.apple.android.music.data.local.room.entity.PlaylistEntity
import com.apple.android.music.data.local.room.entity.PlaylistSongEntity
import com.apple.android.music.domain.model.Playlist
import com.apple.android.music.domain.model.Song
import com.apple.android.music.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PlaylistRepository using Room database
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val songDao: com.apple.android.music.data.local.room.dao.SongDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                // This might be inefficient for lists. Ideally Room relation.
                // But for now keeping logic same.
                // songIds was unused here as we are not fetching songs for the list view
                // We physically cannot do suspending fetch inside map without 'mapLatest' or 'transform'.
                // AND fetching songs inside mapping of playlists is N+1 query problem.
                // Better approach: Just return basic playlist info here? 
                // Playlist model has 'songs: List<Song>'.
                // If we want detailed list, it's heavy.
                // Let's defer song loading or use empty list for list view, and load details in GetPlaylistUseCase.
                // For now, let's load empty list or handle it differently. 
                // Actually, existing code tried to do it.
                // Let's use a simplified approach: Playlists in ListView don't need all songs, just count?
                // The Domain Model requires List<Song>.
                // I'll return empty list here and fix GetPlaylistById to load songs.
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    songs = emptyList() // Optimization: Don't load all songs for all playlists in list view
                )
            }
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(playlistId) ?: return null
        val songIds = playlistDao.getSongIdsForPlaylist(playlistId)
        val songs = getSongsFromIds(songIds)
        
        return Playlist(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt,
            songs = songs
        )
    }

    override suspend fun createPlaylist(name: String): Long {
        val entity = PlaylistEntity(name = name)
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            createdAt = playlist.createdAt
        )
        playlistDao.updatePlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        val playlistSong = PlaylistSongEntity(
            playlistId = playlistId,
            songId = songId,
            position = maxPosition + 1
        )
        playlistDao.addSongToPlaylist(playlistSong)
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        val playlistSongs = songIds.mapIndexed { index, songId ->
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = maxPosition + 1 + index
            )
        }
        playlistDao.addSongsToPlaylist(playlistSongs)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun clearPlaylist(playlistId: Long) {
        playlistDao.clearPlaylist(playlistId)
    }

    override suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean {
        return playlistDao.isSongInPlaylist(playlistId, songId)
    }

    override suspend fun reorderSongs(playlistId: Long, songId: Long, newPosition: Int) {
        playlistDao.updateSongPosition(playlistId, songId, newPosition)
    }

    private suspend fun getSongsFromIds(songIds: List<Long>): List<Song> {
        // Efficient way: Fetch all songs with these IDs from Room
        // SongDao needs getSongsByIds(ids). If not present, loop.
        // Assuming we add getSongsByIds or just loop (slow but working for offline small db).
        // Let's loop for now as we didn't add the method to DAO,
        // OR add the method to DAO. Adding to DAO is better.
        // I will presume I can add to DAO or use loop.
        // Loop:
        val songs = java.util.ArrayList<Song>()
        for (id in songIds) {
             val entity = songDao.getSongById(id)
             entity?.let { songs.add(it.toDomain()) }
        }
        return songs
    }
}

// Mapper extension needed here or import
private fun com.apple.android.music.data.local.room.entity.SongEntity.toDomain(): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        uri = android.net.Uri.parse(path),
        albumId = albumId ?: -1L,
        albumArtUri = artworkUri?.let { android.net.Uri.parse(it) }
    )
}
