package com.apple.android.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.apple.android.music.ui.screens.album.AlbumDetailScreen
import com.apple.android.music.ui.screens.home.HomeScreen
import com.apple.android.music.ui.screens.library.LibraryScreen
import com.apple.android.music.ui.screens.settings.SettingsScreen
import com.apple.android.music.ui.screens.timeline.TimelineScreen

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "library",
        modifier = modifier
    ) {
        
        composable("library") {
            LibraryScreen(
                onNavigateTo = { route -> navController.navigate(route) },
                onAlbumClick = onAlbumClick
            )
        }

        composable("playlists") {
            com.apple.android.music.ui.screens.playlists.PlaylistsScreen(
                onBackClick = { navController.popBackStack() },
                onPlaylistClick = onPlaylistClick
            )
        }

        composable("artists") {
            com.apple.android.music.ui.screens.artists.ArtistsScreen(
                onBackClick = { navController.popBackStack() },
                onArtistClick = onArtistClick
            )
        }

        composable("albums") {
            com.apple.android.music.ui.screens.albums.AlbumsScreen(
                onBackClick = { navController.popBackStack() },
                onAlbumClick = onAlbumClick
            )
        }

        composable("songs") {
            com.apple.android.music.ui.screens.songs.SongsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "album/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) {
            AlbumDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("timeline") {
            TimelineScreen()
        }
        
        composable("settings") {
            SettingsScreen()
        }
    }
}
