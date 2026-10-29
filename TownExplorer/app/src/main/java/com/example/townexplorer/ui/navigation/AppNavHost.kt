package com.example.townexplorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.townexplorer.ui.screens.HomeScreen
import com.example.townexplorer.ui.screens.ProfileScreen
import com.example.townexplorer.ui.screens.QuestsScreen

//defines how users navigate between different screens and determines which screen to display based on the current route.
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.QUESTS -> QuestsScreen()
                    Destination.HOME -> HomeScreen()
                    Destination.PROFILE -> ProfileScreen("first", "last", "email")
                }
            }
        }
    }
}