package com.music.player.ui.screens.library;

import com.music.player.domain.repository.MusicRepository;
import com.music.player.domain.repository.PlaylistRepository;
import com.music.player.player.MusicServiceConnection;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class LibraryViewModel_Factory implements Factory<LibraryViewModel> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  private final Provider<PlaylistRepository> playlistRepositoryProvider;

  private final Provider<MusicServiceConnection> musicServiceConnectionProvider;

  public LibraryViewModel_Factory(Provider<MusicRepository> musicRepositoryProvider,
      Provider<PlaylistRepository> playlistRepositoryProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
    this.playlistRepositoryProvider = playlistRepositoryProvider;
    this.musicServiceConnectionProvider = musicServiceConnectionProvider;
  }

  @Override
  public LibraryViewModel get() {
    return newInstance(musicRepositoryProvider.get(), playlistRepositoryProvider.get(), musicServiceConnectionProvider.get());
  }

  public static LibraryViewModel_Factory create(Provider<MusicRepository> musicRepositoryProvider,
      Provider<PlaylistRepository> playlistRepositoryProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider) {
    return new LibraryViewModel_Factory(musicRepositoryProvider, playlistRepositoryProvider, musicServiceConnectionProvider);
  }

  public static LibraryViewModel newInstance(MusicRepository musicRepository,
      PlaylistRepository playlistRepository, MusicServiceConnection musicServiceConnection) {
    return new LibraryViewModel(musicRepository, playlistRepository, musicServiceConnection);
  }
}
