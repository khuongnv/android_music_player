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
public final class GetArtistsUseCase_Factory implements Factory<GetArtistsUseCase> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  public GetArtistsUseCase_Factory(Provider<MusicRepository> musicRepositoryProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
  }

  @Override
  public GetArtistsUseCase get() {
    return newInstance(musicRepositoryProvider.get());
  }

  public static GetArtistsUseCase_Factory create(
      Provider<MusicRepository> musicRepositoryProvider) {
    return new GetArtistsUseCase_Factory(musicRepositoryProvider);
  }

  public static GetArtistsUseCase newInstance(MusicRepository musicRepository) {
    return new GetArtistsUseCase(musicRepository);
  }
}
