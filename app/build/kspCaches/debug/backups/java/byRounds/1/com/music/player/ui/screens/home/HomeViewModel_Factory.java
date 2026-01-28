package com.music.player.ui.screens.home;

import com.music.player.domain.repository.MusicRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  private final Provider<MusicServiceConnection> musicServiceConnectionProvider;

  public HomeViewModel_Factory(Provider<MusicRepository> musicRepositoryProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
    this.musicServiceConnectionProvider = musicServiceConnectionProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(musicRepositoryProvider.get(), musicServiceConnectionProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<MusicRepository> musicRepositoryProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider) {
    return new HomeViewModel_Factory(musicRepositoryProvider, musicServiceConnectionProvider);
  }

  public static HomeViewModel newInstance(MusicRepository musicRepository,
      MusicServiceConnection musicServiceConnection) {
    return new HomeViewModel(musicRepository, musicServiceConnection);
  }
}
