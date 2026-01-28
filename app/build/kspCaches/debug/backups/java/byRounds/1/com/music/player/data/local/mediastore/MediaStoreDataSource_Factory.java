package com.music.player.data.local.mediastore;

import android.content.ContentResolver;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MediaStoreDataSource_Factory implements Factory<MediaStoreDataSource> {
  private final Provider<ContentResolver> contentResolverProvider;

  public MediaStoreDataSource_Factory(Provider<ContentResolver> contentResolverProvider) {
    this.contentResolverProvider = contentResolverProvider;
  }

  @Override
  public MediaStoreDataSource get() {
    return newInstance(contentResolverProvider.get());
  }

  public static MediaStoreDataSource_Factory create(
      Provider<ContentResolver> contentResolverProvider) {
    return new MediaStoreDataSource_Factory(contentResolverProvider);
  }

  public static MediaStoreDataSource newInstance(ContentResolver contentResolver) {
    return new MediaStoreDataSource(contentResolver);
  }
}
