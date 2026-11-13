package com.example.townexplorer.ui.components

import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


// ================================================================================================
// Shared UI pieces for the Login, Sign up and Forgot password screens, so the three screens look
// identical.
// ===============================================================================================

/**
 * Width of the fields, title and buttons as a fraction of the screen width.
 *
 * Using Modifier.fillMaxWidth(fraction)
 *   Source: https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/fillMaxWidth.modifier
 */
const val AUTH_CONTENT_WIDTH = 0.65f

/**
 * @param title - text shown as the big heading.
 * @param content - the fields, buttons and links of the screen (placed in a Column).
 */
@Composable
fun AuthScreenLayout(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()

            // Keeps content clear of the status bar, navigation bar, display cutout and the on-screen
            // keyboard. MainActivity calls enableEdgeToEdge(), so the app draws behind the system bars
            // and we have to handle insets ourselves.
            //      Source: https://developer.android.com/develop/ui/compose/system/insets-ui
            //      ("Use Compose APIs" - Modifier.safeDrawingPadding(), equivalent to
            //      windowInsetsPadding(WindowInsets.safeDrawing); includes the IME)
            .safeDrawingPadding()

            // Lets the user scroll to the fields when the keyboard covers part of the screen.
            //      Source: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/scroll
            //      (verticalScroll + rememberScrollState())
            .verticalScroll(rememberScrollState()),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Spacer = empty space whose size is set with Modifier.height / width.
        //   Source: https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/Spacer.composable
        Spacer(modifier = Modifier.height(96.dp))
        Text(
            text = title,
            // displaySmall = "the smallest display text" of the Material 3 type scale
            //   Source: https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-typography/
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(AUTH_CONTENT_WIDTH)
        )
        Spacer(modifier = Modifier.height(56.dp))
        content()
    }
}


/**
 * Filled text field with a clear (x) button. Used for the e-mail and password inputs.
 *
 * It is stateless (state hoisting): it shows `value` and reports edits through `onValueChange`; the screen that
 * uses it owns the state.
 *   Source: https://developer.android.com/develop/ui/compose/state  ("State hoisting")
 *
 * @param label - hint text shown inside the field (e.g. "e-mail").
 * @param isPassword - hides the typed characters and asks the keyboard for a password input.
 * @param errorMessage - if not null, the field turns red and shows this text under the input line.
 * @param onDone - if not null, the keyboard shows a "Done" key that calls this.
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    onDone: (() -> Unit)? = null
) {
    // TextField parameters used below (label, trailingIcon, supportingText, isError, visualTransformation,
    // keyboardOptions, keyboardActions, singleLine, colors):
    //   Source: https://developer.android.com/reference/kotlin/androidx/compose/material3/TextField.composable
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(AUTH_CONTENT_WIDTH),
        label = { Text(label) },
        singleLine = true,
        isError = errorMessage != null,

        // Material guidance: the error message appears below the input line and REPLACES the helper
        // text until the problem is fixed - that is what supportingText is used for here.
        //   Source: https://m2.material.io/go/design-text-fields
        supportingText = if (errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        trailingIcon = {
            // Clear button. IconButton + Icon: https://developer.android.com/reference/kotlin/androidx/compose/material/icons/package-summary
            //   (lists androidx.compose.material3.Icon and IconButton)
            // Icons.Default.Clear: Icons.Default is an alias for Icons.Filled
            //   Source: https://developer.android.com/reference/kotlin/androidx/compose/material/icons/Icons
            IconButton(onClick = { onValueChange("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear $label"
                )
            }
        },

        // Hides the typed characters for the password field.
        //   Source: https://developer.android.com/develop/ui/compose/text/user-input
        //   ("Format input" / PasswordTextField sample - PasswordVisualTransformation())
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,

        // KeyboardOptions(keyboardType, imeAction): the same guide lists the supported keyboard options
        // (capitalization, autoCorrect, keyboardType, imeAction) and shows KeyboardType.Password.
        //   Source: https://developer.android.com/develop/ui/compose/text/user-input
        // keyboardActions(onDone) is the callback invoked when the IME emits the Done action (TextField reference).
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),

        // Colours: TextFieldDefaults.colors() lets us override per-state colours.
        //   Source: https://developer.android.com/reference/kotlin/androidx/compose/material3/TextFieldDefaults
        //           https://developer.android.com/reference/kotlin/androidx/compose/material3/TextFieldColors
        // Why onPrimary as the field background: in Material 3, "on<colour>" is meant for text/icons placed ON
        // that colour (see the colour-role table in https://goo.gle/apply-dynamic-color)
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            errorContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}


/**
 * Filled (high-emphasis) button used for "Log in", "Sign up" and "Send reset link".
 *
 * Button's default container/content colours are primary/onPrimary.
 *   Source: https://developer.android.com/develop/ui/compose/components/button
 *           https://developer.android.com/reference/kotlin/androidx/compose/material3/Button.composable
 * `shape` is a documented Button parameter. An example of Button with `shape = RoundedCornerShape(10.dp)`:
 *   Source: https://gist.github.com/parishsu/bc86038b6ffd2025dcca49088859ecb7
 *
 * @param text - label of the button.
 * @param onClick - called when the button is pressed.
 * @param enabled - false disables the button.
 */
@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text)
    }
}


/**
 * Basic e-mail FORMAT check using the platform's own pattern. Whether the account exists is the backend's job.
 *   Source: https://developer.android.com/reference/android/util/Patterns  (Patterns.EMAIL_ADDRESS)
 * Checking the format before calling the backend is also what this Firebase walkthrough recommends:
 *   Source: https://itnext.io/android-firebase-authentication-email-and-password-login-b06980cf864a
 *
 * @return True if `email` looks like an e-mail address.
 */
fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()