package com.music.player.ui.screens.albums;

import com.music.player.domain.usecase.GetAlbumsUseCase;
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
public final class AlbumsViewModel_Factory implements Factory<AlbumsViewModel> {
  private final Provider<GetAlbumsUseCase> getAlbumsUseCaseProvider;

  public AlbumsViewModel_Factory(Provider<GetAlbumsUseCase> getAlbumsUseCaseProvider) {
    this.getAlbumsUseCaseProvider = getAlbumsUseCaseProvider;
  }

  @Override
  public AlbumsViewModel get() {
    return newInstance(getAlbumsUseCaseProvider.get());
  }

  public static AlbumsViewModel_Factory create(
      Provider<GetAlbumsUseCase> getAlbumsUseCaseProvider) {
    return new AlbumsViewModel_Factory(getAlbumsUseCaseProvider);
  }

  public static AlbumsViewModel newInstance(GetAlbumsUseCase getAlbumsUseCase) {
    return new AlbumsViewModel(getAlbumsUseCase);
  }
}
