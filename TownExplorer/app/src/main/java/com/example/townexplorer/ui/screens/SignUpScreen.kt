package com.example.townexplorer.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.townexplorer.ui.components.AUTH_CONTENT_WIDTH
import com.example.townexplorer.ui.components.AuthButton
import com.example.townexplorer.ui.components.AuthScreenLayout
import com.example.townexplorer.ui.components.AuthTextField
import com.example.townexplorer.ui.components.isValidEmail


/**
 * Sign up screen
 *
 * User journey: "Enters e-mail" -> "Enters password" -> "Clicks 'Sign up'".
 * If the backend says "A user already exists that is using this e-mail", the user stays on this screen and goes
 * back to entering the e-mail. The `errorMessage` parameter is the CALLER (which
 * talks to the backend), it passes the message in, and the screen shows it under the e-mail field. Typically backends
 * report this case with an Exception such as Firebase's FirebaseAuthUserCollisionException ("email address is
 * currently in use by another account"):
 *   Source: https://itnext.io/android-firebase-authentication-email-and-password-login-b06980cf864a
 *
 * This is state hoisting: the screen displays state it is given and reports events upward.
 *   Source: https://developer.android.com/develop/ui/compose/state  ("State hoisting")
 *
 * @param onSignUp called with the trimmed e-mail and the password once both pass the basic checks.
 * @param errorMessage backend error to show under the e-mail field (null = none).
 * @param onEmailChanged called whenever the e-mail text changes, so the caller can clear a stale errorMessage.
 */
@Composable
fun SignUpScreen(
    onSignUp: (email: String, password: String) -> Unit,
    errorMessage: String? = null,
    onEmailChanged: () -> Unit = {}
) {
    // rememberSaveable: see LoginScreen.kt for the source.
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    // Errors only appear after the first "Sign up" press (Material guidance, see LoginScreen.kt).
    var submitted by rememberSaveable { mutableStateOf(false) }

    // The password field only checks for "not empty"; real password rules belong
    // to the backend (e.g. Firebase rejects passwords shorter than 6 characters with FirebaseAuthWeakPasswordException,
    //      per the ITNEXT article above).
    // `a ?: b` (Elvis operator) = use `a` if it is not null, otherwise `b`:
    //      Source: https://kotlinlang.org/docs/null-safety.html
    val emailFormatError = if (submitted && !isValidEmail(email.trim())) "Enter a valid e-mail address" else null
    val passwordError = if (submitted && password.isEmpty()) "Choose a password" else null

    // Runs when the user presses "Sign up" or the keyboard's Done key: marks the form as submitted and, if the
    // inputs are valid, reports the sign-up event upward.
    val submit: () -> Unit = {
        submitted = true
        if (isValidEmail(email.trim()) && password.isNotEmpty()) {
            onSignUp(email.trim(), password)
        }
    }

    AuthScreenLayout(title = "Sign up") {
        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                onEmailChanged()
            },
            label = "e-mail",
            // A format error from this screen wins; otherwise show the backend's "already exists" message.
            errorMessage = emailFormatError ?: errorMessage
        )
        Spacer(modifier = Modifier.height(24.dp))

        // "Create a password" caption above the password field
        // bodyLarge is the default body style
        Text(
            text = "Create a password",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(AUTH_CONTENT_WIDTH)
        )
        Spacer(modifier = Modifier.height(4.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "password",
            isPassword = true,
            errorMessage = passwordError,
            onDone = submit
        )

        Spacer(modifier = Modifier.height(40.dp))
        AuthButton(text = "Sign up", onClick = submit)
    }
}