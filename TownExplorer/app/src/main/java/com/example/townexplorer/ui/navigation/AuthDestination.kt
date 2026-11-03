package com.example.townexplorer.ui.navigation

/**
 * Routes for the screens shown BEFORE the user is logged in: Login, Sign up and Forgot password.
 *
 * Kept separate from Destination.kt on purpose: Destination is used to build the bottom navigation bar
 * (NavigationBar.kt), and these three screens must not show up in that bar.
 *
 * Pattern (an enum with a String route per destination, and String routes passed to NavHost/navigate):
 *   Source: https://developer.android.com/guide/navigation/navigation-navigate
 *           ("Navigate to a composable" - navigate(route) takes a String route)
 *   The enum-with-route idea mirrors Destination.kt, which follows
 *   https://developer.android.com/develop/ui/compose/components/navigation-bar
 */
enum class AuthDestination(val route: String) {
    LOGIN("login"),
    SIGN_UP("sign_up"),
    FORGOT_PASSWORD("forgot_password")
}