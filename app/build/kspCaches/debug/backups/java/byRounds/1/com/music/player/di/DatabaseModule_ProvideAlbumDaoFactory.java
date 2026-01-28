package com.music.player.di;

import com.music.player.data.local.room.MusicDatabase;
import com.music.player.data.local.room.dao.AlbumDao;
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
public final class DatabaseModule_ProvideAlbumDaoFactory implements Factory<AlbumDao> {
  private final Provider<MusicDatabase> databaseProvider;

  public DatabaseModule_ProvideAlbumDaoFactory(Provider<MusicDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AlbumDao get() {
    return provideAlbumDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideAlbumDaoFactory create(
      Provider<MusicDatabase> databaseProvider) {
    return new DatabaseModule_ProvideAlbumDaoFactory(databaseProvider);
  }

  public static AlbumDao provideAlbumDao(MusicDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAlbumDao(database));
  }
}
