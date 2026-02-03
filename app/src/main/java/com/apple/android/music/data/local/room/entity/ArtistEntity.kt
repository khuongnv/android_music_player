package com.apple.android.music.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val numberOfTracks: Int = 0,
    val numberOfAlbums: Int = 0
)
