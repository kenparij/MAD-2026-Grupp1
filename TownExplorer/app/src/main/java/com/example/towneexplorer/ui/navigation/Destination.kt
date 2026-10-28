package com.example.towneexplorer.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    //val icon: ImageVector,
    val contentDescription: String
) {
    QUESTS("quests", "Quests", "Quests"),
    HOME("home", "Home", "Home"),
    PROFILE("profile", "Profile", "Profile")
}