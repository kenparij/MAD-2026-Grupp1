package com.example.townexplorer.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.townexplorer.ui.components.AuthButton
import com.example.townexplorer.ui.components.AuthScreenLayout
import com.example.townexplorer.ui.components.AuthTextField
import com.example.townexplorer.ui.components.isValidEmail


/**
 * Forgot password screen: Log in screen -> "Clicks 'Forgot password?'"
 * -> "Email is sent" -> back to the Log in screen.
 *
 *   - "Send reset link" -> onSendResetLink(email); the caller sends the e-mail and returns to Log in
 *   - "Back to login screen" -> onBackToLogin()
 *
 * Only events are exposed; the screen never touches the NavController.
 *   Source: https://developer.android.com/guide/navigation/navigation-navigate  ("Expose events from your composables")
 *
 * @param onSendResetLink - called with the trimmed e-mail once it passes the basic format check.
 * @param onBackToLogin - called when "Back to login screen" is pressed.
 */
@Composable
fun ForgotPasswordScreen(
    onSendResetLink: (email: String) -> Unit,
    onBackToLogin: () -> Unit
) {
    // rememberSaveable: see LoginScreen.kt for the source.
    var email by rememberSaveable { mutableStateOf("") }
    // Errors only appear after the first press (Material guidance, see LoginScreen.kt).
    var submitted by rememberSaveable { mutableStateOf(false) }

    // format check = Patterns.EMAIL_ADDRESS (see isValidEmail in AuthComponents.kt).
    val emailError = if (submitted && !isValidEmail(email.trim())) "Enter a valid e-mail address" else null

    // Runs when the user presses "Send reset link" or the keyboard's Done key: marks the form as submitted and,
    // if the e-mail looks valid, reports the send-reset-link event upward.
    val submit: () -> Unit = {
        submitted = true
        if (isValidEmail(email.trim())) {
            onSendResetLink(email.trim())
        }
    }

    AuthScreenLayout(title = "Forgot password") {
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "e-mail",
            errorMessage = emailError,
            onDone = submit
        )
        Spacer(modifier = Modifier.height(40.dp))
        AuthButton(text = "Send reset link", onClick = submit)
        Spacer(modifier = Modifier.height(8.dp))
        // TextButton: low-emphasis action (see LoginScreen.kt for sources).
        TextButton(onClick = onBackToLogin) {
            Text("Back to login screen")
        }
    }
}