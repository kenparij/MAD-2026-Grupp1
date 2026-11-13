package com.example.townexplorer.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.townexplorer.data.AuthError
import com.example.townexplorer.data.AuthRepository
import com.example.townexplorer.data.UserProfile
import com.example.townexplorer.data.UserRepository
import com.example.townexplorer.ui.screens.ForgotPasswordScreen
import com.example.townexplorer.ui.screens.HomeScreen
import com.example.townexplorer.ui.screens.LoginScreen
import com.example.townexplorer.ui.screens.ProfileScreen
import com.example.townexplorer.ui.screens.QuestsScreen
import com.example.townexplorer.ui.screens.SignUpScreen
import kotlinx.coroutines.launch


/**
 * Defines how users navigate between different screens and determines which screen to display based on the current route.
 *
 * NavHost with String routes + composable(route):
 *    Source: https://developer.android.com/guide/navigation/navigation-navigate  ("Navigate to a composable")
 *
 * @param navController - the controller that owns the back stack (created in NavigationBar.kt).
 * @param startDestination - route of the first screen shown.
 *
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    // context is needed to show a Toast
    val context = LocalContext.current
    // Scope for the Firebase calls
    val scope = rememberCoroutineScope()

    NavHost(
        navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ---- Auth flow ----

        // Login screen route.
        composable(AuthDestination.LOGIN.route) {

            // Holds the backend error for invalid credentials
            // remember + mutableStateOf:
            //   Source: https://developer.android.com/develop/ui/compose/state
            var loginError by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(false) }

            LoginScreen(
                errorMessage = loginError,
                isLoading = isLoading,

                // Clears the old error as soon as the user edits the e-mail or password.
                onCredentialsChanged = { loginError = null },

                // "Logs in" -> Map screen (home).
                // popUpTo(LOGIN) { inclusive = true } removes the login screen from the back stack, so the
                // Android back button does not take the user back into the login flow.
                //   Source: https://developer.android.com/guide/navigation/backstack
                //   (Compose example `navigate("destination_b") { popUpTo("destination_a") { inclusive = true } }
                onLogIn = { email, password ->
                    scope.launch {
                        isLoading = true
                        val error = AuthRepository.signIn(email, password)
                        isLoading = false
                        if (error == null) {
                            navController.navigate(Destination.HOME.route) {
                                popUpTo(AuthDestination.LOGIN.route) { inclusive = true }
                            }
                        } else {
                            loginError = error.message
                        }
                    }
                },
                // "Clicks 'Forgot password?'" -> Forgot password screen.
                onForgotPassword = { navController.navigate(AuthDestination.FORGOT_PASSWORD.route) },
                // "Clicks 'Sign up'" -> Sign up screen.
                onCreateAccount = { navController.navigate(AuthDestination.SIGN_UP.route) }
            )
        }

        // Sign up screen route.
        composable(AuthDestination.SIGN_UP.route) {
            // Holds the backend error ("A user already exists that is using this e-mail")
            // remember + mutableStateOf:
            //   Source: https://developer.android.com/develop/ui/compose/state
            var signUpError by remember { mutableStateOf<String?>(null) }
            var passwordError by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(false) }

            SignUpScreen(
                errorMessage = signUpError,
                passwordErrorMessage = passwordError,
                isLoading = isLoading,

                // Clears the old errors as soon as the user edits a field.
                onEmailChanged = { signUpError = null },
                onPasswordChanged = { passwordError = null },
                onSignUp = { email, password ->
                    scope.launch {
                        isLoading = true
                        val error = AuthRepository.signUp(email, password)
                        isLoading = false
                        when (error) {
                            // Account created -> back to the Log in screen.
                            // popBackStack(route, inclusive = false) pops everything above the Login screen and
                            // keeps Login itself.
                            //   Source: https://developer.android.com/guide/navigation/backstack
                            //   ("Pop back to a particular destination")
                            null -> {
                                Toast.makeText(context, "Account created. You can log in now.", Toast.LENGTH_SHORT)
                                    .show()
                                navController.popBackStack(AuthDestination.LOGIN.route, inclusive = false)
                            }
                            AuthError.WeakPassword -> passwordError = error.message
                            else -> signUpError = error.message
                        }
                    }
                }
            )
        }

        // Forgot password screen route.
        composable(AuthDestination.FORGOT_PASSWORD.route) {
            var resetError by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(false) }

            ForgotPasswordScreen(
                errorMessage = resetError,
                isLoading = isLoading,
                onEmailChanged = { resetError = null },
                onSendResetLink = { email ->
                    scope.launch {
                        isLoading = true
                        val error = AuthRepository.sendPasswordReset(email)
                        isLoading = false
                        if (error == null) {
                            // "Email is sent" -> back to the Log in screen. A Toast tells the user what
                            // happened; it stays visible while the screen changes.
                            //   Source: https://developer.android.com/guide/topics/ui/notifiers/toasts
                            //   (Toast.makeText(context, text, duration).show())
                            Toast.makeText(
                                context,
                                "If an account exists for $email, a reset link was sent",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack(AuthDestination.LOGIN.route, inclusive = false)
                        } else {
                            resetError = error.message
                        }
                    }
                },
                // "Back to login screen" link, same pop as above, without the Toast.
                onBackToLogin = {
                    navController.popBackStack(AuthDestination.LOGIN.route, inclusive = false)
                }
            )
        }

        // ---- Main app ----
        // Registers one route per bottom-bar Destination. `entries` is the list of all enum values:
        //   Source: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.enums/-enum-entries/
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.QUESTS -> QuestsScreen()
                    Destination.HOME -> HomeScreen()
                    Destination.PROFILE -> {
                        val user = AuthRepository.currentUser
                        var profile by remember { mutableStateOf<UserProfile?>(null) }

                        // Load the profile from Firestore
                        LaunchedEffect(user?.uid) {
                            if (user != null) profile = UserRepository.getProfile(user.uid)
                        }

                        ProfileScreen(
                            displayName = profile?.displayName,
                            email = profile?.email ?: user?.email.orEmpty(),
                            onLogOut = {
                                // Log out and clear the back stack
                                AuthRepository.signOut()
                                navController.navigate(AuthDestination.LOGIN.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
