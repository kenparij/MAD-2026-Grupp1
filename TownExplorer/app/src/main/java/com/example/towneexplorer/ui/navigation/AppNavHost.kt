package com.example.towneexplorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.towneexplorer.ui.screens.HomeScreen
import com.example.towneexplorer.ui.screens.ProfileScreen
import com.example.towneexplorer.ui.screens.questsScreen

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
                    Destination.QUESTS -> questsScreen()
                    Destination.HOME -> HomeScreen()
                    Destination.PROFILE -> ProfileScreen()
                }
            }
        }
    }
}