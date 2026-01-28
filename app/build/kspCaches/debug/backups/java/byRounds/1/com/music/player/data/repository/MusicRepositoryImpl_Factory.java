package com.music.player.data.repository;

import com.music.player.data.local.room.dao.AlbumDao;
import com.music.player.data.local.room.dao.ArtistDao;
import com.music.player.data.local.room.dao.SongDao;
import com.music.player.data.local.scanner.MusicScanner;
import com.music.player.domain.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class MusicRepositoryImpl_Factory implements Factory<MusicRepositoryImpl> {
  private final Provider<SongDao> songDaoProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  private final Provider<ArtistDao> artistDaoProvider;

  private final Provider<MusicScanner> musicScannerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public MusicRepositoryImpl_Factory(Provider<SongDao> songDaoProvider,
      Provider<AlbumDao> albumDaoProvider, Provider<ArtistDao> artistDaoProvider,
      Provider<MusicScanner> musicScannerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.songDaoProvider = songDaoProvider;
    this.albumDaoProvider = albumDaoProvider;
    this.artistDaoProvider = artistDaoProvider;
    this.musicScannerProvider = musicScannerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public MusicRepositoryImpl get() {
    return newInstance(songDaoProvider.get(), albumDaoProvider.get(), artistDaoProvider.get(), musicScannerProvider.get(), settingsRepositoryProvider.get());
  }

  public static MusicRepositoryImpl_Factory create(Provider<SongDao> songDaoProvider,
      Provider<AlbumDao> albumDaoProvider, Provider<ArtistDao> artistDaoProvider,
      Provider<MusicScanner> musicScannerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new MusicRepositoryImpl_Factory(songDaoProvider, albumDaoProvider, artistDaoProvider, musicScannerProvider, settingsRepositoryProvider);
  }

  public static MusicRepositoryImpl newInstance(SongDao songDao, AlbumDao albumDao,
      ArtistDao artistDao, MusicScanner musicScanner, SettingsRepository settingsRepository) {
    return new MusicRepositoryImpl(songDao, albumDao, artistDao, musicScanner, settingsRepository);
  }
}
