package com.music.player.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.music.player.data.local.mediastore.MediaStoreDataSource
import com.music.player.data.local.room.MusicDatabase
import com.music.player.data.local.room.dao.AlbumDao
import com.music.player.data.local.room.dao.ArtistDao
import com.music.player.data.local.room.dao.PlaylistDao
import com.music.player.data.local.room.dao.SongDao
import com.music.player.data.repository.MusicRepositoryImpl
import com.music.player.data.repository.PlaylistRepositoryImpl
import com.music.player.domain.repository.MusicRepository
import com.music.player.domain.repository.PlaylistRepository
import com.music.player.domain.repository.PlayerStateRepository
import com.music.player.domain.repository.SettingsRepository
import com.music.player.data.repository.PlayerStateRepositoryImpl
import com.music.player.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            MusicDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: MusicDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    @Singleton
    fun provideAlbumDao(database: MusicDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    @Singleton
    fun provideArtistDao(database: MusicDatabase): ArtistDao {
        return database.artistDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindPlayerStateRepository(impl: PlayerStateRepositoryImpl): PlayerStateRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
