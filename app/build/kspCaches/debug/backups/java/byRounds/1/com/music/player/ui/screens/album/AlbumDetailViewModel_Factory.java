package com.music.player.ui.screens.album;

import androidx.lifecycle.SavedStateHandle;
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
public final class AlbumDetailViewModel_Factory implements Factory<AlbumDetailViewModel> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  private final Provider<MusicServiceConnection> musicServiceConnectionProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AlbumDetailViewModel_Factory(Provider<MusicRepository> musicRepositoryProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
    this.musicServiceConnectionProvider = musicServiceConnectionProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AlbumDetailViewModel get() {
    return newInstance(musicRepositoryProvider.get(), musicServiceConnectionProvider.get(), savedStateHandleProvider.get());
  }

  public static AlbumDetailViewModel_Factory create(
      Provider<MusicRepository> musicRepositoryProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AlbumDetailViewModel_Factory(musicRepositoryProvider, musicServiceConnectionProvider, savedStateHandleProvider);
  }

  public static AlbumDetailViewModel newInstance(MusicRepository musicRepository,
      MusicServiceConnection musicServiceConnection, SavedStateHandle savedStateHandle) {
    return new AlbumDetailViewModel(musicRepository, musicServiceConnection, savedStateHandle);
  }
}
