package com.example.townexplorer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//Displays the quests screen, showing quests as dropdown menus
@Composable
fun QuestsScreen(){
    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quests", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(16.dp))
        DropdownMenu("Quest 1", "First quest")
        DropdownMenu("Quest 2", "Second quest")
    }
}


//dropdown menu component for displaying quests
@Composable
fun DropdownMenu(
    title: String,
    description: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(0.6f)
            .padding(8.dp)
            .clickable{isExpanded = !isExpanded},
        colors = CardColors(MaterialTheme.colorScheme.onPrimary,
            Color.Black,
            MaterialTheme.colorScheme.onPrimary,
            Color.Black),
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.onPrimary)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                Text(title, fontSize = 20.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(
                isExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 700)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 700)
                ),
                modifier = Modifier.background(MaterialTheme.colorScheme.onPrimaryContainer)
                    .fillMaxWidth()
                    .defaultMinSize(0.dp, 100.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(description)
                }
            }
        }
    }
}