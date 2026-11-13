package com.example.townexplorer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.townexplorer.ui.components.AUTH_CONTENT_WIDTH
import com.example.townexplorer.ui.components.AuthButton
import com.example.townexplorer.ui.components.AuthScreenLayout
import com.example.townexplorer.ui.components.AuthTextField
import com.example.townexplorer.ui.components.isValidEmail

/**
 *
 * The screen can do 3 things:
 *   - "Logs in" -> onLogIn(email, password)  (caller then opens Map/Home)
 *   - "Clicks 'Forgot password?'" -> onForgotPassword()
 *   - "Clicks 'Sign up'" -> onCreateAccount()
 *
 * When the backend rejects the e-mail/password, the CALLER passes a message in `errorMessage`
 * and the screen shows it in red under the password field.
 *
 * The screen never touches the NavController; it only exposes events and the NavHost decides what
 * happens (see AppNavHost.kt).
 *   Source: https://developer.android.com/guide/navigation/navigation-navigate  ("Expose events from your composables")
 *
 * @param onLogIn - called with the trimmed e-mail and the password once both pass the basic checks.
 * @param onForgotPassword - called when "Forgot password?" is pressed.
 * @param onCreateAccount - called when "Create account" is pressed.
 * @param errorMessage - backend "invalid credentials" message to show under the password field (null = none).
 * @param onCredentialsChanged - called whenever the e-mail or password text changes, so the caller can clear a
 *        stale errorMessage (the error should disappear once the user starts correcting the input).
 * @param isLoading - disables "Log in" while a request is running.
 */
@Composable
fun LoginScreen(
    onLogIn: (email: String, password: String) -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    errorMessage: String? = null,
    onCredentialsChanged: () -> Unit = {},
    isLoading: Boolean = false
) {
    // rememberSaveable keeps what the user typed across configuration changes.
    //   Source: https://developer.android.com/develop/ui/compose/state
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // Errors are only shown after the first press of "Log in", and disappear as soon as the input is valid.
    var submitted by rememberSaveable { mutableStateOf(false) }

    // String.trim() removes leading/trailing whitespace, so the e-mail check does not fail because of it.
    //   Source: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/trim.html
    val emailError = if (submitted && !isValidEmail(email.trim())) "Enter a valid e-mail address" else null
    val passwordError = if (submitted && password.isEmpty()) "Enter your password" else null

    // Runs when the user presses "Log in" or the keyboard's Done key: marks the form as submitted and, if the
    // inputs are valid, reports the log-in event upward.
    val submit: () -> Unit = {
        submitted = true
        if (!isLoading && isValidEmail(email.trim()) && password.isNotEmpty()) {
            onLogIn(email.trim(), password)
        }
    }

    AuthScreenLayout(title = "Log In") {
        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                onCredentialsChanged()
            },
            label = "e-mail",
            errorMessage = emailError
        )
        Spacer(modifier = Modifier.height(24.dp))
        AuthTextField(
            value = password,
            onValueChange = {
                password = it
                onCredentialsChanged()
            },
            label = "password",
            isPassword = true,
            errorMessage = passwordError,
            onDone = submit
        )

        // "Incorrect e-mail or password" small red text, left-aligned under the password field.
        // colorScheme.error is the Material 3 role "used to indicate errors in components,
        // such as invalid text in a text field"
        //   Source: https://developer.android.com/reference/kotlin/androidx/tv/material3/ColorScheme
        //   (same role description; the phone version is https://developer.android.com/reference/kotlin/androidx/compose/material3/ColorScheme)
        //
        // It is a separate Text (not the TextField's supportingText) so the fields keep their normal underline
        if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxWidth(AUTH_CONTENT_WIDTH),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // "Forgot password?" link, left-aligned under the password field
        // Box(contentAlignment = ...) aligns its child inside it:
        //   Source: https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/fillMaxWidth.modifier
        //
        //   Source: https://developer.android.com/reference/kotlin/androidx/compose/material3/TextButton.composable
        //           https://developer.android.com/develop/ui/compose/components/button
        Box(
            modifier = Modifier.fillMaxWidth(AUTH_CONTENT_WIDTH),
            contentAlignment = Alignment.CenterStart
        ) {
            TextButton(
                onClick = onForgotPassword,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Forgot password?", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        AuthButton(text = "Log in", onClick = submit, enabled = !isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onCreateAccount) {
            Text("Create account")
        }
    }
}