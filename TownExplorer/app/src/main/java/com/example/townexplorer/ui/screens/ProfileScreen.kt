package com.example.townexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.townexplorer.ui.theme.Error
import com.example.townexplorer.ui.theme.PurpleGrey40

/**
 * Displays the user's profile, quest progress, and account actions
 */
@Composable
fun ProfileScreen(
    firstName: String,
    lastName: String,
    email: String,
    completedQuests: Int = 2,
    totalQuests: Int = 5,
    onLogOut: () -> Unit = {}
) {
    val progress = if (totalQuests > 0) completedQuests.toFloat() / totalQuests.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Avatar
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Username
        Text(
            text = "$firstName $lastName",
            style = MaterialTheme.typography.titleLarge
        )

        // User Email
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Quest Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Quest Progress",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completed: $completedQuests / $totalQuests",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = PurpleGrey40
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Log out button
        OutlinedButton(
            onClick = onLogOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Log out",
                color = Error,
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    MaterialTheme {
        ProfileScreen(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            completedQuests = 3,
            totalQuests = 6,
            onLogOut = {}
        )
    }
}
