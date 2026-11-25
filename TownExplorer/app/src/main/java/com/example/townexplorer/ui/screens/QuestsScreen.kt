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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.townexplorer.data.AuthRepository
import com.example.townexplorer.data.AuthRepository.currentUser
import com.example.townexplorer.data.PoiRepository
import com.example.townexplorer.data.PointOfInterest
import com.example.townexplorer.data.UserRepository

data class CategoryQuest(
    val type: String,
    val pois: List<PointOfInterest>,
    val visitedIds: Set<String>
) {
    val visitedCount: Int = pois.count { it.id in visitedIds }
    val totalCount: Int = pois.size
    val isCompleted: Boolean = totalCount > 0 && visitedCount == totalCount
}

//Displays the quests screen, showing quests as dropdown menus
@Composable
fun QuestsScreen() {
    val currentUser = currentUser

    var pois by remember { mutableStateOf<List<PointOfInterest>>(emptyList()) }
    var visitedPoiIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        pois = PoiRepository.getAllPois()
    }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            visitedPoiIds = UserRepository.getVisitedPoiIds(uid)
        }
    }

    // Source: https://developer.android.com/develop/ui/compose/side-effects
    val quests = remember(pois, visitedPoiIds) {
        buildCategoryQuests(pois, visitedPoiIds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quests", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(quests, key = { it.type }) { quest ->
                val displayType = quest.type.lowercase().replaceFirstChar { it.uppercase() }
                DropdownMenu(
                    title = "Visit every $displayType POI",
                    quest = quest
                )
            }
        }
    }
}


//dropdown menu component for displaying quests
@Composable
fun DropdownMenu(
    title: String,
    quest: CategoryQuest
) {
    var isExpanded by remember { mutableStateOf(false) }
    val progress = if (quest.totalCount == 0) 0f else quest.visitedCount.toFloat() / quest.totalCount

    Card(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .padding(8.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = Color.Black
        ),
        border = BorderStroke(
            width = 3.dp,
            color = if (quest.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (quest.isCompleted) {
                            "${quest.visitedCount} / ${quest.totalCount} - Completed"
                        } else {
                            "${quest.visitedCount} / ${quest.totalCount} visited"
                        },
                        fontSize = 14.sp,
                        color = if (quest.isCompleted) Color(0xFF059669) else Color.DarkGray
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 700)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 700)
                ),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 80.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    quest.pois.forEach { poi ->
                        val visited = poi.id in quest.visitedIds
                        Text(
                            text = if (visited) "✓ ${poi.name}" else "○ ${poi.name}",
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Takes a list of points of interest and a set of POI ID-s the user has visited,
// and turns them into a sorted list of CategoryQuest objects
// Source: https://kotlinlang.org/docs/collection-grouping.html
//         https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/group-by.html
//         https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/sorted-by.html
fun buildCategoryQuests(
    pois: List<PointOfInterest>,
    visitedPoiIds: Set<String>
): List<CategoryQuest> {
    return pois.filter { it.type.isNotBlank() }
        .groupBy { it.type.trim() }
        .map { (type, poisInCategory) ->
            CategoryQuest(
                type = type,
                pois = poisInCategory,
                visitedIds = visitedPoiIds
            )
        }
        .sortedBy { it.type }
}