package com.example.townexplorer.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.townexplorer.ui.screens.ForgotPasswordScreen
import com.example.townexplorer.ui.screens.HomeScreen
import com.example.townexplorer.ui.screens.LoginScreen
import com.example.townexplorer.ui.screens.ProfileScreen
import com.example.townexplorer.ui.screens.QuestsScreen
import com.example.townexplorer.ui.screens.SignUpScreen


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

    NavHost(
        navController,
        startDestination = startDestination
    ) {
        // ---- Auth flow (from our user journey diagram) ----

        // Login screen route.
        composable(AuthDestination.LOGIN.route) {
            // Holds the backend error for invalid credentials
            // remember + mutableStateOf:
            //   Source: https://developer.android.com/develop/ui/compose/state
            var loginError by remember { mutableStateOf<String?>(null) }

            LoginScreen(
                errorMessage = loginError,

                // Clears the old error as soon as the user edits the e-mail or password.
                onCredentialsChanged = { loginError = null },

                // "Logs in" -> Map screen (home).
                // popUpTo(LOGIN) { inclusive = true } removes the login screen from the back stack, so the
                // Android back button does not take the user back into the login flow.
                //   Source: https://developer.android.com/guide/navigation/backstack
                //   (Compose example `navigate("destination_b") { popUpTo("destination_a") { inclusive = true } }
                onLogIn = { _, _ ->
                    // TODO (placeholder): authenticate against backend first, and only navigate on
                    // success (Firebase example: signInWithEmailAndPassword).
                    //      Source: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
                    navController.navigate(Destination.HOME.route) {
                        popUpTo(AuthDestination.LOGIN.route) { inclusive = true }
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

            SignUpScreen(
                errorMessage = signUpError,

                // Clears the old error as soon as the user edits the e-mail.
                onEmailChanged = { signUpError = null },
                onSignUp = { _, _ ->
                    // TODO (placeholder): call backend here (Firebase example: createUserWithEmailAndPassword).
                    // If the e-mail is already registered (Firebase throws FirebaseAuthUserCollisionException,
                    // https://itnext.io/android-firebase-authentication-email-and-password-login-b06980cf864a),
                    // show error and stay on this screen:
                    //     signUpError = "A user already exists that is using this e-mail"
                    // On success (below) go back to the Log in screen.
                    //
                    // popBackStack(route, inclusive = false) pops everything above the Login screen and keeps
                    // Login itself.
                    //   Source: https://developer.android.com/guide/navigation/backstack
                    //   ("Pop back to a particular destination")
                    navController.popBackStack(AuthDestination.LOGIN.route, inclusive = false)
                }
            )
        }

        // Forgot password screen route.
        composable(AuthDestination.FORGOT_PASSWORD.route) {
            ForgotPasswordScreen(
                onSendResetLink = { email ->
                    // TODO (placeholder): actually send the reset e-mail via backend
                    // (Firebase example: sendPasswordResetEmail).
                    //
                    // Diagram: "Email is sent" -> back to the Log in screen. A Toast tells the user what
                    // happened; it stays visible while the screen changes.
                    //   Source: https://developer.android.com/guide/topics/ui/notifiers/toasts
                    //   (Toast.makeText(context, text, duration).show())
                    Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
                    navController.popBackStack(AuthDestination.LOGIN.route, inclusive = false)
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
                    Destination.PROFILE -> ProfileScreen("first", "last", "email")
                }
            }
        }
    }
}
