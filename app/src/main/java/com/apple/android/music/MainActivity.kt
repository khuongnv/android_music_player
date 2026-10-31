package com.apple.android.music

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apple.android.music.ui.navigation.NavGraph
import com.apple.android.music.ui.components.BottomNavBar
import com.apple.android.music.ui.components.MiniPlayer
import com.apple.android.music.ui.screens.home.HomeViewModel
import com.apple.android.music.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.apple.android.music.domain.model.ThemeMode
import com.apple.android.music.domain.repository.SettingsRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        Log.d("MusicApp", "Permissions granted: $granted")
        // Ideally we'd trigger a refresh here if we had access to the VM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MusicApp", "MainActivity onCreate started")
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("MusicApp", "CRASH in thread ${thread.name}", throwable)
        }

        try {
            setContent {
                val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
                val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                val isDarkTheme = when (themeMode) {
                    ThemeMode.SYSTEM -> isSystemDark
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }

                MusicPlayerTheme(darkTheme = isDarkTheme) {
                    val navController = rememberNavController()
                    
                    // Check permissions
                    LaunchedEffect(Unit) {
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        requestPermissionLauncher.launch(permissions)
                    }

                    // Using HomeViewModel for global player state for now
                    // In a larger app, this might be a dedicated PlayerViewModel
                    val playerViewModel: HomeViewModel = hiltViewModel()

                    // Save state when app is backgrounded
                    DisposableEffect(lifecycle) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_STOP) {
                                Log.d("MusicApp", "MainActivity: ON_STOP - Saving state")
                                playerViewModel.saveState()
                            }
                        }
                        lifecycle.addObserver(observer)
                        onDispose {
                            lifecycle.removeObserver(observer)
                        }
                    }

                    Surface(modifier = Modifier.fillMaxSize()) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route ?: "home"
                        
                        val currentSong by playerViewModel.currentSong.collectAsState()
                        val isPlaying by playerViewModel.isPlaying.collectAsState()
                        val currentPosition by playerViewModel.currentPosition.collectAsState()
                        val duration by playerViewModel.duration.collectAsState()
                        
                        var isPlayerExpanded by remember { mutableStateOf(false) }

                        // Handle Back Press when player is expanded
                        androidx.activity.compose.BackHandler(enabled = isPlayerExpanded) {
                            isPlayerExpanded = false
                        }
                        
                        // Return to Library tab when pressing Back from Timeline or Settings tabs
                        val isNonLibraryTab = currentRoute == "timeline" || currentRoute == "settings"
                        androidx.activity.compose.BackHandler(enabled = !isPlayerExpanded && isNonLibraryTab) {
                            navController.navigate("library") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }

                        val onNavigateTab: (String) -> Unit = remember(navController) {
                            { route ->
                                Log.d("MusicApp", "Navigating to $route")
                                if (isPlayerExpanded) isPlayerExpanded = false // Collapse if navigating
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            Scaffold(
                                bottomBar = {
                                    Column {
                                        // Global Mini Player
                                        MiniPlayer(
                                            currentSong = currentSong,
                                            isPlaying = isPlaying,
                                            currentPosition = currentPosition,
                                            duration = duration,
                                            onPlayPauseClick = { playerViewModel.togglePlayPause() },
                                            onPreviousClick = { playerViewModel.playPrevious() },
                                            onNextClick = { playerViewModel.playNext() },
                                            onClick = { isPlayerExpanded = true },
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .padding(bottom = 6.dp)
                                        )
                                        
                                        BottomNavBar(
                                            currentRoute = currentRoute,
                                            onNavigate = onNavigateTab
                                        )
                                    }
                                }
                            ) { paddingValues ->
                                NavGraph(
                                    navController = navController,
                                    onAlbumClick = { albumId -> 
                                        navController.navigate("album/$albumId")
                                    },
                                    onArtistClick = { _ -> navController.navigate("songs") }, // Placeholder
                                    onPlaylistClick = { _ -> navController.navigate("songs") }, // Placeholder
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                )
                            }
                            
                            // Full Screen Player Overlay
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isPlayerExpanded,
                                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                com.apple.android.music.ui.screens.player.NowPlayingScreen(
                                    onCollapse = { isPlayerExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicApp", "Critical UI initialization error", e)
        }
    }
}
