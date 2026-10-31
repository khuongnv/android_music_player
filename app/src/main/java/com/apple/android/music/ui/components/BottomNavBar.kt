package com.apple.android.music.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apple.android.music.ui.theme.AppleRed
import com.apple.android.music.ui.theme.DarkBackground
import com.apple.android.music.ui.theme.Gray93

/**
 * Bottom navigation destinations with refined Apple Music labels and aesthetics
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Library : BottomNavItem(
        route = "library",
        title = "Thư viện",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    )

    object Memories : BottomNavItem(
        route = "timeline",
        title = "Ký ức",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "Cài đặt",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Library,
    BottomNavItem.Memories,
    BottomNavItem.Settings
)

/**
 * Liquid Glass Floating Bottom Navigation Bar:
 * - Floating dock design with large rounded corners (26dp)
 * - Translucent crystalline/smoky glass material with ambient rim highlight
 * - Active tab highlighted with an animated Liquid Glass capsule pill (bo tròn icon & label)
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    // Liquid Glass color tokens
    val glassBg = if (isDark) {
        Color(0xEE1E1E22) // Translucent smoky dark glass
    } else {
        Color(0xF2FFFFFF) // Crystalline translucent white glass
    }

    val glassBorder = if (isDark) {
        Color(0x33FFFFFF) // Luminous silver rim
    } else {
        Color(0x1F000000) // Subtle shadow rim
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 16.dp else 12.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = if (isDark) Color(0x66000000) else Color(0x26000000)
                )
                .border(
                    BorderStroke(0.75.dp, glassBorder),
                    shape = RoundedCornerShape(26.dp)
                ),
            shape = RoundedCornerShape(26.dp),
            color = glassBg,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    // Animated pill background for the active tab
                    val pillBg by animateColorAsState(
                        targetValue = if (isSelected) AppleRed.copy(alpha = 0.12f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "PillBg"
                    )

                    val pillBorder by animateColorAsState(
                        targetValue = if (isSelected) AppleRed.copy(alpha = 0.28f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "PillBorder"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBg)
                            .border(BorderStroke(0.5.dp, pillBorder), RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigate(item.route) }
                            )
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) AppleRed else Gray93,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppleRed else Gray93,
                                letterSpacing = (-0.2).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
