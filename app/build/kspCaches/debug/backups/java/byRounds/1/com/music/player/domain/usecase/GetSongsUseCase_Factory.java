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
public final class GetSongsUseCase_Factory implements Factory<GetSongsUseCase> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  public GetSongsUseCase_Factory(Provider<MusicRepository> musicRepositoryProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
  }

  @Override
  public GetSongsUseCase get() {
    return newInstance(musicRepositoryProvider.get());
  }

  public static GetSongsUseCase_Factory create(Provider<MusicRepository> musicRepositoryProvider) {
    return new GetSongsUseCase_Factory(musicRepositoryProvider);
  }

  public static GetSongsUseCase newInstance(MusicRepository musicRepository) {
    return new GetSongsUseCase(musicRepository);
  }
}
