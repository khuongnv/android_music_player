package com.music.player.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [Index(value = ["path"], unique = true)]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val path: String,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val artworkUri: String? = null,
    val albumId: Long? = null, // Foreign key to AlbumEntity if strictly enforcing relational
    val artistId: Long? = null // Foreign key to ArtistEntity
)
