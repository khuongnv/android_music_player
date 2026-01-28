package com.music.player.ui.screens.songs;

import com.music.player.domain.usecase.GetSongsUseCase;
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
public final class SongsViewModel_Factory implements Factory<SongsViewModel> {
  private final Provider<GetSongsUseCase> getSongsUseCaseProvider;

  private final Provider<MusicServiceConnection> musicServiceConnectionProvider;

  public SongsViewModel_Factory(Provider<GetSongsUseCase> getSongsUseCaseProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider) {
    this.getSongsUseCaseProvider = getSongsUseCaseProvider;
    this.musicServiceConnectionProvider = musicServiceConnectionProvider;
  }

  @Override
  public SongsViewModel get() {
    return newInstance(getSongsUseCaseProvider.get(), musicServiceConnectionProvider.get());
  }

  public static SongsViewModel_Factory create(Provider<GetSongsUseCase> getSongsUseCaseProvider,
      Provider<MusicServiceConnection> musicServiceConnectionProvider) {
    return new SongsViewModel_Factory(getSongsUseCaseProvider, musicServiceConnectionProvider);
  }

  public static SongsViewModel newInstance(GetSongsUseCase getSongsUseCase,
      MusicServiceConnection musicServiceConnection) {
    return new SongsViewModel(getSongsUseCase, musicServiceConnection);
  }
}
