package com.music.player;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.music.player.data.local.room.MusicDatabase;
import com.music.player.data.local.room.dao.AlbumDao;
import com.music.player.data.local.room.dao.ArtistDao;
import com.music.player.data.local.room.dao.PlaylistDao;
import com.music.player.data.local.room.dao.SongDao;
import com.music.player.data.local.scanner.MusicScanner;
import com.music.player.data.repository.MusicRepositoryImpl;
import com.music.player.data.repository.PlayerStateRepositoryImpl;
import com.music.player.data.repository.PlaylistRepositoryImpl;
import com.music.player.data.repository.SettingsRepositoryImpl;
import com.music.player.di.DatabaseModule_ProvideAlbumDaoFactory;
import com.music.player.di.DatabaseModule_ProvideArtistDaoFactory;
import com.music.player.di.DatabaseModule_ProvideDatabaseFactory;
import com.music.player.di.DatabaseModule_ProvidePlaylistDaoFactory;
import com.music.player.di.DatabaseModule_ProvideSongDaoFactory;
import com.music.player.domain.usecase.GetAlbumsUseCase;
import com.music.player.domain.usecase.GetArtistsUseCase;
import com.music.player.domain.usecase.GetSongsUseCase;
import com.music.player.player.MusicService;
import com.music.player.player.MusicServiceConnection;
import com.music.player.player.MusicService_MembersInjector;
import com.music.player.ui.screens.album.AlbumDetailViewModel;
import com.music.player.ui.screens.album.AlbumDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.albums.AlbumsViewModel;
import com.music.player.ui.screens.albums.AlbumsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.artists.ArtistsViewModel;
import com.music.player.ui.screens.artists.ArtistsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.home.HomeViewModel;
import com.music.player.ui.screens.home.HomeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.library.LibraryViewModel;
import com.music.player.ui.screens.library.LibraryViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.nowplaying.NowPlayingViewModel;
import com.music.player.ui.screens.nowplaying.NowPlayingViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.playlists.PlaylistsViewModel;
import com.music.player.ui.screens.playlists.PlaylistsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.search.SearchViewModel;
import com.music.player.ui.screens.search.SearchViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.settings.SettingsViewModel;
import com.music.player.ui.screens.settings.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.music.player.ui.screens.songs.SongsViewModel;
import com.music.player.ui.screens.songs.SongsViewModel_HiltModules_KeyModule_ProvideFactory;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerMusicApp_HiltComponents_SingletonC {
  private DaggerMusicApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public MusicApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements MusicApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public MusicApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements MusicApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public MusicApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements MusicApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public MusicApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements MusicApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MusicApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements MusicApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MusicApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements MusicApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public MusicApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements MusicApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public MusicApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends MusicApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends MusicApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends MusicApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends MusicApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return ImmutableSet.<String>of(AlbumDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide(), AlbumsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ArtistsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), HomeViewModel_HiltModules_KeyModule_ProvideFactory.provide(), LibraryViewModel_HiltModules_KeyModule_ProvideFactory.provide(), NowPlayingViewModel_HiltModules_KeyModule_ProvideFactory.provide(), com.music.player.ui.screens.player.NowPlayingViewModel_HiltModules_KeyModule_ProvideFactory.provide(), PlaylistsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SearchViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SongsViewModel_HiltModules_KeyModule_ProvideFactory.provide());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends MusicApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AlbumDetailViewModel> albumDetailViewModelProvider;

    private Provider<AlbumsViewModel> albumsViewModelProvider;

    private Provider<ArtistsViewModel> artistsViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<LibraryViewModel> libraryViewModelProvider;

    private Provider<NowPlayingViewModel> nowPlayingViewModelProvider;

    private Provider<com.music.player.ui.screens.player.NowPlayingViewModel> nowPlayingViewModelProvider2;

    private Provider<PlaylistsViewModel> playlistsViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SongsViewModel> songsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private GetAlbumsUseCase getAlbumsUseCase() {
      return new GetAlbumsUseCase(singletonCImpl.musicRepositoryImplProvider.get());
    }

    private GetArtistsUseCase getArtistsUseCase() {
      return new GetArtistsUseCase(singletonCImpl.musicRepositoryImplProvider.get());
    }

    private GetSongsUseCase getSongsUseCase() {
      return new GetSongsUseCase(singletonCImpl.musicRepositoryImplProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.albumDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.albumsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.artistsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.libraryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.nowPlayingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.nowPlayingViewModelProvider2 = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.playlistsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.songsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<String, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(11).put("com.music.player.ui.screens.album.AlbumDetailViewModel", ((Provider) albumDetailViewModelProvider)).put("com.music.player.ui.screens.albums.AlbumsViewModel", ((Provider) albumsViewModelProvider)).put("com.music.player.ui.screens.artists.ArtistsViewModel", ((Provider) artistsViewModelProvider)).put("com.music.player.ui.screens.home.HomeViewModel", ((Provider) homeViewModelProvider)).put("com.music.player.ui.screens.library.LibraryViewModel", ((Provider) libraryViewModelProvider)).put("com.music.player.ui.screens.nowplaying.NowPlayingViewModel", ((Provider) nowPlayingViewModelProvider)).put("com.music.player.ui.screens.player.NowPlayingViewModel", ((Provider) nowPlayingViewModelProvider2)).put("com.music.player.ui.screens.playlists.PlaylistsViewModel", ((Provider) playlistsViewModelProvider)).put("com.music.player.ui.screens.search.SearchViewModel", ((Provider) searchViewModelProvider)).put("com.music.player.ui.screens.settings.SettingsViewModel", ((Provider) settingsViewModelProvider)).put("com.music.player.ui.screens.songs.SongsViewModel", ((Provider) songsViewModelProvider)).build();
    }

    @Override
    public Map<String, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<String, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.music.player.ui.screens.album.AlbumDetailViewModel 
          return (T) new AlbumDetailViewModel(singletonCImpl.musicRepositoryImplProvider.get(), singletonCImpl.musicServiceConnectionProvider.get(), viewModelCImpl.savedStateHandle);

          case 1: // com.music.player.ui.screens.albums.AlbumsViewModel 
          return (T) new AlbumsViewModel(viewModelCImpl.getAlbumsUseCase());

          case 2: // com.music.player.ui.screens.artists.ArtistsViewModel 
          return (T) new ArtistsViewModel(viewModelCImpl.getArtistsUseCase());

          case 3: // com.music.player.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.musicRepositoryImplProvider.get(), singletonCImpl.musicServiceConnectionProvider.get());

          case 4: // com.music.player.ui.screens.library.LibraryViewModel 
          return (T) new LibraryViewModel(singletonCImpl.musicRepositoryImplProvider.get(), singletonCImpl.playlistRepositoryImplProvider.get(), singletonCImpl.musicServiceConnectionProvider.get());

          case 5: // com.music.player.ui.screens.nowplaying.NowPlayingViewModel 
          return (T) new NowPlayingViewModel(singletonCImpl.musicServiceConnectionProvider.get());

          case 6: // com.music.player.ui.screens.player.NowPlayingViewModel 
          return (T) new com.music.player.ui.screens.player.NowPlayingViewModel(singletonCImpl.musicServiceConnectionProvider.get());

          case 7: // com.music.player.ui.screens.playlists.PlaylistsViewModel 
          return (T) new PlaylistsViewModel(singletonCImpl.playlistRepositoryImplProvider.get());

          case 8: // com.music.player.ui.screens.search.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.musicRepositoryImplProvider.get(), singletonCImpl.musicServiceConnectionProvider.get());

          case 9: // com.music.player.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.musicRepositoryImplProvider.get(), singletonCImpl.settingsRepositoryImplProvider.get());

          case 10: // com.music.player.ui.screens.songs.SongsViewModel 
          return (T) new SongsViewModel(viewModelCImpl.getSongsUseCase(), singletonCImpl.musicServiceConnectionProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends MusicApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends MusicApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectMusicService(MusicService musicService) {
      injectMusicService2(musicService);
    }

    private MusicService injectMusicService2(MusicService instance) {
      MusicService_MembersInjector.injectPlayerStateRepository(instance, singletonCImpl.playerStateRepositoryImplProvider.get());
      MusicService_MembersInjector.injectMusicRepository(instance, singletonCImpl.musicRepositoryImplProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends MusicApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<MusicDatabase> provideDatabaseProvider;

    private Provider<SongDao> provideSongDaoProvider;

    private Provider<AlbumDao> provideAlbumDaoProvider;

    private Provider<ArtistDao> provideArtistDaoProvider;

    private Provider<SettingsRepositoryImpl> settingsRepositoryImplProvider;

    private Provider<MusicRepositoryImpl> musicRepositoryImplProvider;

    private Provider<MusicServiceConnection> musicServiceConnectionProvider;

    private Provider<PlaylistDao> providePlaylistDaoProvider;

    private Provider<PlaylistRepositoryImpl> playlistRepositoryImplProvider;

    private Provider<PlayerStateRepositoryImpl> playerStateRepositoryImplProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private MusicScanner musicScanner() {
      return new MusicScanner(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule), provideSongDaoProvider.get(), provideAlbumDaoProvider.get(), provideArtistDaoProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<MusicDatabase>(singletonCImpl, 2));
      this.provideSongDaoProvider = DoubleCheck.provider(new SwitchingProvider<SongDao>(singletonCImpl, 1));
      this.provideAlbumDaoProvider = DoubleCheck.provider(new SwitchingProvider<AlbumDao>(singletonCImpl, 3));
      this.provideArtistDaoProvider = DoubleCheck.provider(new SwitchingProvider<ArtistDao>(singletonCImpl, 4));
      this.settingsRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepositoryImpl>(singletonCImpl, 5));
      this.musicRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<MusicRepositoryImpl>(singletonCImpl, 0));
      this.musicServiceConnectionProvider = DoubleCheck.provider(new SwitchingProvider<MusicServiceConnection>(singletonCImpl, 6));
      this.providePlaylistDaoProvider = DoubleCheck.provider(new SwitchingProvider<PlaylistDao>(singletonCImpl, 8));
      this.playlistRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<PlaylistRepositoryImpl>(singletonCImpl, 7));
      this.playerStateRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<PlayerStateRepositoryImpl>(singletonCImpl, 9));
    }

    @Override
    public void injectMusicApp(MusicApp musicApp) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.music.player.data.repository.MusicRepositoryImpl 
          return (T) new MusicRepositoryImpl(singletonCImpl.provideSongDaoProvider.get(), singletonCImpl.provideAlbumDaoProvider.get(), singletonCImpl.provideArtistDaoProvider.get(), singletonCImpl.musicScanner(), singletonCImpl.settingsRepositoryImplProvider.get());

          case 1: // com.music.player.data.local.room.dao.SongDao 
          return (T) DatabaseModule_ProvideSongDaoFactory.provideSongDao(singletonCImpl.provideDatabaseProvider.get());

          case 2: // com.music.player.data.local.room.MusicDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.music.player.data.local.room.dao.AlbumDao 
          return (T) DatabaseModule_ProvideAlbumDaoFactory.provideAlbumDao(singletonCImpl.provideDatabaseProvider.get());

          case 4: // com.music.player.data.local.room.dao.ArtistDao 
          return (T) DatabaseModule_ProvideArtistDaoFactory.provideArtistDao(singletonCImpl.provideDatabaseProvider.get());

          case 5: // com.music.player.data.repository.SettingsRepositoryImpl 
          return (T) new SettingsRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.music.player.player.MusicServiceConnection 
          return (T) new MusicServiceConnection(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.music.player.data.repository.PlaylistRepositoryImpl 
          return (T) new PlaylistRepositoryImpl(singletonCImpl.providePlaylistDaoProvider.get(), singletonCImpl.provideSongDaoProvider.get());

          case 8: // com.music.player.data.local.room.dao.PlaylistDao 
          return (T) DatabaseModule_ProvidePlaylistDaoFactory.providePlaylistDao(singletonCImpl.provideDatabaseProvider.get());

          case 9: // com.music.player.data.repository.PlayerStateRepositoryImpl 
          return (T) new PlayerStateRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
