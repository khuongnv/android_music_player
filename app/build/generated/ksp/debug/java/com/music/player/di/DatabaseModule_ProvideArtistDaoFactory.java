package com.music.player.di;

import com.music.player.data.local.room.MusicDatabase;
import com.music.player.data.local.room.dao.ArtistDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DatabaseModule_ProvideArtistDaoFactory implements Factory<ArtistDao> {
  private final Provider<MusicDatabase> databaseProvider;

  public DatabaseModule_ProvideArtistDaoFactory(Provider<MusicDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ArtistDao get() {
    return provideArtistDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideArtistDaoFactory create(
      Provider<MusicDatabase> databaseProvider) {
    return new DatabaseModule_ProvideArtistDaoFactory(databaseProvider);
  }

  public static ArtistDao provideArtistDao(MusicDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideArtistDao(database));
  }
}
