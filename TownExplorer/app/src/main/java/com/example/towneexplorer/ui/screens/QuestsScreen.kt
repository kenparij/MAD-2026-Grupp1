package com.example.towneexplorer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun questsScreen(){
    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quests", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(16.dp))
        dropdownMenu("Quest 1", "First quest")
        dropdownMenu("Quest 2", "Second quest")
    }
}


@Composable
fun dropdownMenu(
    title: String,
    description: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(0.6f)
            .padding(8.dp)
            .clickable{isExpanded = !isExpanded}

    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                Text(title, fontSize = 20.sp)
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
                modifier = Modifier.background(Color.LightGray)
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