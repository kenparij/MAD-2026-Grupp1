package com.example.townexplorer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
/**
 * Bundles each screen's navigation route together with the visual properties needed to render bottom navigation bar.
 * Code example taken from https://developer.android.com/develop/ui/compose/components/navigation-bar
 */
enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    QUESTS("quests", "Quests", Icons.Default.Star,"Quests"),
    HOME("home", "Home", Icons.Default.LocationOn, "Home"),
    PROFILE("profile", "Profile", Icons.Default.AccountCircle, "Profile")
}