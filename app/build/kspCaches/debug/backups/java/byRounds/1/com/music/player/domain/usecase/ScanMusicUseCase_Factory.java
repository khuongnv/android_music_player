package com.music.player.domain.usecase;

import com.music.player.domain.repository.MusicRepository;
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
public final class ScanMusicUseCase_Factory implements Factory<ScanMusicUseCase> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  public ScanMusicUseCase_Factory(Provider<MusicRepository> musicRepositoryProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
  }

  @Override
  public ScanMusicUseCase get() {
    return newInstance(musicRepositoryProvider.get());
  }

  public static ScanMusicUseCase_Factory create(Provider<MusicRepository> musicRepositoryProvider) {
    return new ScanMusicUseCase_Factory(musicRepositoryProvider);
  }

  public static ScanMusicUseCase newInstance(MusicRepository musicRepository) {
    return new ScanMusicUseCase(musicRepository);
  }
}
