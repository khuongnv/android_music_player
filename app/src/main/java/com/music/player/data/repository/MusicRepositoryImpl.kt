package com.music.player.data.repository

import android.net.Uri
import android.util.Log
import com.music.player.data.local.room.dao.AlbumDao
import com.music.player.data.local.room.dao.ArtistDao
import com.music.player.data.local.room.dao.SongDao
import com.music.player.data.local.scanner.MusicScanner
import com.music.player.domain.model.Album
import com.music.player.domain.model.Artist
import com.music.player.domain.model.Song
import com.music.player.domain.repository.MusicRepository
import com.music.player.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val musicScanner: MusicScanner,
    private val settingsRepository: SettingsRepository
) : MusicRepository {

    override suspend fun getAllSongs(): List<Song> {
        return songDao.getAllSongs().first().map { it.toDomain() }
    }

    override fun getSongsFlow(): Flow<List<Song>> = songDao.getAllSongs().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun getSongById(songId: Long): Song? {
        return songDao.getSongById(songId)?.toDomain()
    }

    override suspend fun getSongsForAlbum(albumId: Long): List<Song> {
        return songDao.getSongsByAlbum(albumId).first().map { it.toDomain() }
    }

    override suspend fun getSongsForArtist(artistName: String): List<Song> {
        // We need artistId. But the interface takes artistName.
        // We can look up Artist by name first.
        val artist = artistDao.getArtistByName(artistName) ?: return emptyList()
        return songDao.getSongsByArtist(artist.id).first().map { it.toDomain() }
    }

    override suspend fun getAllAlbums(): List<Album> {
        return albumDao.getAllAlbums().first().map { it.toDomain() }
    }

    override fun getAlbumsFlow(): Flow<List<Album>> = albumDao.getAllAlbums().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun getAlbumById(albumId: Long): Album? {
        return albumDao.getAlbumById(albumId)?.toDomain()
    }

    override suspend fun getAllArtists(): List<Artist> {
        // We need to calculate songCount if not stored. ArtistEntity has numberOfTracks.
        // Assuming MusicScanner updates it or we calculate it here?
        // Scanner didn't explicitly update artist counts.
        // Ideally we query DB for count.
        // For now, mapping directly.
        return artistDao.getAllArtists().first().map { it.toDomain() }
    }

    override fun getArtistsFlow(): Flow<List<Artist>> = artistDao.getAllArtists().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun refreshMusic() {
        val folders = settingsRepository.getSelectedFolders()
        // If folders is a list of strings (URI strings)
        Log.d("MusicRepository", "Scanning folders: $folders")
        folders.forEach { uriString ->
             try {
                 val uri = Uri.parse(uriString)
                 musicScanner.scanDirectory(uri)
             } catch (e: Exception) {
                 Log.e("MusicRepository", "Error parsing URI: $uriString", e)
             }
        }
    }

    override suspend fun getMusicFolders(): List<String> {
        // Return folders from Settings 
        return settingsRepository.getSelectedFolders().toList()
    }

    // Mappers
    private fun com.music.player.data.local.room.entity.SongEntity.toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            uri = Uri.parse(path),
            albumId = albumId ?: -1,
            albumArtUri = artworkUri?.let { Uri.parse(it) }
        )
    }

    private fun com.music.player.data.local.room.entity.AlbumEntity.toDomain(): Album {
        return Album(
            id = id,
            name = title,
            artist = artist,
            songCount = 0, // TODO: Implement count query
            albumArtUri = artworkPath?.let { Uri.parse(it) }
        )
    }

    private fun com.music.player.data.local.room.entity.ArtistEntity.toDomain(): Artist {
        return Artist(
            id = id,
            name = name,
            songCount = numberOfTracks,
            albumCount = numberOfAlbums
        )
    }
}
