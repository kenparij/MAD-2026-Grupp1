package com.example.townexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//Displays the user's profile
@Composable
fun ProfileScreen(
    firstName: String,
    lastName: String,
    email: String
){
    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AccountCircle,
            contentDescription = "placeholder",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(164.dp))
        Text("First name Last name", style = MaterialTheme.typography.titleLarge)
        Text("email@email.com", style = MaterialTheme.typography.bodyLarge)

    }
}