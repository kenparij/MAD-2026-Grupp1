package com.example.townexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays the user's profile
 *
 * @param displayName - the user's name (null if not set).
 * @param email - the user's e-mail.
 * @param onLogOut - called when "Log out" is pressed.
 */
@Composable
fun ProfileScreen(
    displayName: String?,
    email: String,
    onLogOut: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AccountCircle,
            contentDescription = "Profile picture",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(164.dp))
        Text(displayName ?: "Explorer", style = MaterialTheme.typography.titleLarge)
        Text(email, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onLogOut) {
            Text("Log out")
        }
    }
}
