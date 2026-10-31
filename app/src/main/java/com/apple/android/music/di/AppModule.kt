package com.apple.android.music.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.apple.android.music.data.local.mediastore.MediaStoreDataSource
import com.apple.android.music.data.local.room.MusicDatabase
import com.apple.android.music.data.local.room.dao.AlbumDao
import com.apple.android.music.data.local.room.dao.ArtistDao
import com.apple.android.music.data.local.room.dao.PlaylistDao
import com.apple.android.music.data.local.room.dao.SongDao
import com.apple.android.music.data.repository.MusicRepositoryImpl
import com.apple.android.music.data.repository.PlaylistRepositoryImpl
import com.apple.android.music.domain.repository.MusicRepository
import com.apple.android.music.domain.repository.PlaylistRepository
import com.apple.android.music.domain.repository.PlayerStateRepository
import com.apple.android.music.domain.repository.SettingsRepository
import com.apple.android.music.domain.repository.UpdateRepository
import com.apple.android.music.data.repository.PlayerStateRepositoryImpl
import com.apple.android.music.data.repository.SettingsRepositoryImpl
import com.apple.android.music.data.repository.UpdateRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository
}
