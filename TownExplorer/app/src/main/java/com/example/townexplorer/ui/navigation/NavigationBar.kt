package com.example.townexplorer.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState


/**
 * Defines the navigation bar component
 * Code example taken from https://developer.android.com/develop/ui/compose/components/navigation-bar
 */

@Composable
fun NavigationBar(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // TODO: replace with a real "is there a saved session?" check.
    // Source: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
    val isLoggedIn = false
    val startDestination =
        if (isLoggedIn) Destination.HOME.route else AuthDestination.LOGIN.route


    // Observe the current destination to know which screen is showing.
    //   Source: https://developer.android.com/develop/ui/compose/navigation  ("Integration with the bottom nav bar")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    // Only the three main screens get the bottom bar. Hiding the bar by comparing the current route with the
    // list of "tab" routes:
    //   Source: https://medium.com/@hanas.marc/using-lookaheadscope-and-intermediatelayout-for-bottom-bar-inside-compose-navigation-e5abd0cb3cdc
    //           (`val isVisible = currentTab in Tabs.entries.map { it.label }`)
    //           https://slack-chats.kotlinlang.org/t/508901/i-want-to-hide-my-bottom-nav-on-certain-screens-right-now-i-
    // `any { }` returns true if at least one element matches:
    //   Source: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.enums/-enum-entries/
    val showBottomBar = Destination.entries.any { it.route == currentRoute }


    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            // Highlight the tab whose route is the one currently shown.
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(route = destination.route)
                            },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.contentDescription,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            label = { Text(destination.label,
                                color = MaterialTheme.colorScheme.onPrimary) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(navController, startDestination, modifier = Modifier.padding(innerPadding))
    }
}
