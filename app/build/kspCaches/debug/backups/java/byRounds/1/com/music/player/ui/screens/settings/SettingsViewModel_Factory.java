package com.music.player.ui.screens.settings;

import com.music.player.domain.repository.MusicRepository;
import com.music.player.domain.repository.SettingsRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<MusicRepository> musicRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public SettingsViewModel_Factory(Provider<MusicRepository> musicRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.musicRepositoryProvider = musicRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(musicRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<MusicRepository> musicRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SettingsViewModel_Factory(musicRepositoryProvider, settingsRepositoryProvider);
  }

  public static SettingsViewModel newInstance(MusicRepository musicRepository,
      SettingsRepository settingsRepository) {
    return new SettingsViewModel(musicRepository, settingsRepository);
  }
}
