package com.music.player.ui.screens.artists;

import com.music.player.domain.usecase.GetArtistsUseCase;
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
public final class ArtistsViewModel_Factory implements Factory<ArtistsViewModel> {
  private final Provider<GetArtistsUseCase> getArtistsUseCaseProvider;

  public ArtistsViewModel_Factory(Provider<GetArtistsUseCase> getArtistsUseCaseProvider) {
    this.getArtistsUseCaseProvider = getArtistsUseCaseProvider;
  }

  @Override
  public ArtistsViewModel get() {
    return newInstance(getArtistsUseCaseProvider.get());
  }

  public static ArtistsViewModel_Factory create(
      Provider<GetArtistsUseCase> getArtistsUseCaseProvider) {
    return new ArtistsViewModel_Factory(getArtistsUseCaseProvider);
  }

  public static ArtistsViewModel newInstance(GetArtistsUseCase getArtistsUseCase) {
    return new ArtistsViewModel(getArtistsUseCase);
  }
}
