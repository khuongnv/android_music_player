package com.music.player.player;

import com.music.player.domain.repository.MusicRepository;
import com.music.player.domain.repository.PlayerStateRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MusicService_MembersInjector implements MembersInjector<MusicService> {
  private final Provider<PlayerStateRepository> playerStateRepositoryProvider;

  private final Provider<MusicRepository> musicRepositoryProvider;

  public MusicService_MembersInjector(Provider<PlayerStateRepository> playerStateRepositoryProvider,
      Provider<MusicRepository> musicRepositoryProvider) {
    this.playerStateRepositoryProvider = playerStateRepositoryProvider;
    this.musicRepositoryProvider = musicRepositoryProvider;
  }

  public static MembersInjector<MusicService> create(
      Provider<PlayerStateRepository> playerStateRepositoryProvider,
      Provider<MusicRepository> musicRepositoryProvider) {
    return new MusicService_MembersInjector(playerStateRepositoryProvider, musicRepositoryProvider);
  }

  @Override
  public void injectMembers(MusicService instance) {
    injectPlayerStateRepository(instance, playerStateRepositoryProvider.get());
    injectMusicRepository(instance, musicRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.music.player.player.MusicService.playerStateRepository")
  public static void injectPlayerStateRepository(MusicService instance,
      PlayerStateRepository playerStateRepository) {
    instance.playerStateRepository = playerStateRepository;
  }

  @InjectedFieldSignature("com.music.player.player.MusicService.musicRepository")
  public static void injectMusicRepository(MusicService instance, MusicRepository musicRepository) {
    instance.musicRepository = musicRepository;
  }
}
