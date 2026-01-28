package com.music.player.data.local.scanner;

import android.content.Context;
import com.music.player.data.local.room.dao.AlbumDao;
import com.music.player.data.local.room.dao.ArtistDao;
import com.music.player.data.local.room.dao.SongDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class MusicScanner_Factory implements Factory<MusicScanner> {
  private final Provider<Context> contextProvider;

  private final Provider<SongDao> songDaoProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  private final Provider<ArtistDao> artistDaoProvider;

  public MusicScanner_Factory(Provider<Context> contextProvider, Provider<SongDao> songDaoProvider,
      Provider<AlbumDao> albumDaoProvider, Provider<ArtistDao> artistDaoProvider) {
    this.contextProvider = contextProvider;
    this.songDaoProvider = songDaoProvider;
    this.albumDaoProvider = albumDaoProvider;
    this.artistDaoProvider = artistDaoProvider;
  }

  @Override
  public MusicScanner get() {
    return newInstance(contextProvider.get(), songDaoProvider.get(), albumDaoProvider.get(), artistDaoProvider.get());
  }

  public static MusicScanner_Factory create(Provider<Context> contextProvider,
      Provider<SongDao> songDaoProvider, Provider<AlbumDao> albumDaoProvider,
      Provider<ArtistDao> artistDaoProvider) {
    return new MusicScanner_Factory(contextProvider, songDaoProvider, albumDaoProvider, artistDaoProvider);
  }

  public static MusicScanner newInstance(Context context, SongDao songDao, AlbumDao albumDao,
      ArtistDao artistDao) {
    return new MusicScanner(context, songDao, albumDao, artistDao);
  }
}
