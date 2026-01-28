package com.music.player.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PlayerStateRepositoryImpl_Factory implements Factory<PlayerStateRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public PlayerStateRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PlayerStateRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static PlayerStateRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new PlayerStateRepositoryImpl_Factory(contextProvider);
  }

  public static PlayerStateRepositoryImpl newInstance(Context context) {
    return new PlayerStateRepositoryImpl(context);
  }
}
