package com.music.player.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.music.player.data.local.room.dao.AlbumDao
import com.music.player.data.local.room.dao.ArtistDao
import com.music.player.data.local.room.dao.PlaylistDao
import com.music.player.data.local.room.dao.SongDao
import com.music.player.data.local.room.entity.AlbumEntity
import com.music.player.data.local.room.entity.ArtistEntity
import com.music.player.data.local.room.entity.PlaylistEntity
import com.music.player.data.local.room.entity.PlaylistSongEntity
import com.music.player.data.local.room.entity.SongEntity

/**
 * Room database for the music player app
 */
@Database(
    entities = [
        PlaylistEntity::class, 
        PlaylistSongEntity::class,
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    
    companion object {
        const val DATABASE_NAME = "music_player_db"
    }
}
