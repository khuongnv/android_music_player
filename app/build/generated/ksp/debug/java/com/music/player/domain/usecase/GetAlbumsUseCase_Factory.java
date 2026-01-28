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
public final class GetAlbumsUseCase_Factory implements Factory<GetAlbumsUseCase> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  public GetAlbumsUseCase_Factory(Provider<MusicRepository> musicRepositoryProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
  }

  @Override
  public GetAlbumsUseCase get() {
    return newInstance(musicRepositoryProvider.get());
  }

  public static GetAlbumsUseCase_Factory create(Provider<MusicRepository> musicRepositoryProvider) {
    return new GetAlbumsUseCase_Factory(musicRepositoryProvider);
  }

  public static GetAlbumsUseCase newInstance(MusicRepository musicRepository) {
    return new GetAlbumsUseCase(musicRepository);
  }
}
