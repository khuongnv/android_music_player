package com.apple.android.music.data.repository

import android.net.Uri
import android.util.Log
import com.apple.android.music.data.local.room.dao.AlbumDao
import com.apple.android.music.data.local.room.dao.ArtistDao
import com.apple.android.music.data.local.room.dao.SongDao
import com.apple.android.music.data.local.scanner.MusicScanner
import com.apple.android.music.domain.model.Album
import com.apple.android.music.domain.model.Artist
import com.apple.android.music.domain.model.Song
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.SettingsRepository
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
        return artistDao.getAllArtists().first().map { it.toDomain() }
    }

    override fun getArtistsFlow(): Flow<List<Artist>> = artistDao.getAllArtists().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun refreshMusic() {
        val folders = settingsRepository.getSelectedFolders()
        Log.d("MusicRepository", "Scanning folders: $folders")
        if (folders.isNotEmpty()) {
            folders.forEach { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    musicScanner.scanDirectory(uri)
                } catch (e: Exception) {
                    Log.e("MusicRepository", "Error parsing URI: $uriString", e)
                }
            }
        } else {
            // Default: Scan device audio library via MediaStore
            musicScanner.scanMediaStore()
        }
    }

    override suspend fun clearMusicDatabase() {
        musicScanner.clearDatabase()
    }

    override suspend fun getMusicFolders(): List<String> {
        return settingsRepository.getSelectedFolders().toList()
    }

    // Mappers
    private fun com.apple.android.music.data.local.room.entity.SongEntity.toDomain(): Song {
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

    private fun com.apple.android.music.data.local.room.entity.AlbumEntity.toDomain(): Album {
        return Album(
            id = id,
            name = title,
            artist = artist,
            songCount = 0,
            albumArtUri = artworkPath?.let { Uri.parse(it) }
        )
    }

    private fun com.apple.android.music.data.local.room.entity.ArtistEntity.toDomain(): Artist {
        return Artist(
            id = id,
            name = name,
            songCount = numberOfTracks,
            albumCount = numberOfAlbums
        )
    }
}
