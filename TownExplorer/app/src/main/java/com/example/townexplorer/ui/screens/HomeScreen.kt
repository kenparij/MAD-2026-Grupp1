package com.example.townexplorer.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.townexplorer.data.AuthRepository
import com.example.townexplorer.data.PoiRepository
import com.example.townexplorer.data.PointOfInterest
import com.example.townexplorer.data.UserRepository
import com.example.townexplorer.map.MapLibreView
import kotlinx.coroutines.launch


/**
 * Displays the exploration map and the POI bottom sheet.
 *
 * Sources:
 *   https://developer.android.com/develop/ui/compose
 */
@Composable
fun HomeScreen() {
    val coroutineScope = rememberCoroutineScope()
    val currentUser = AuthRepository.currentUser

    var pois by remember { mutableStateOf<List<PointOfInterest>>(emptyList()) }
    var visitedPoiIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedPoi by remember { mutableStateOf<PointOfInterest?>(null) }

    LaunchedEffect(Unit) {
        pois = PoiRepository.getAllPois()
    }
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            visitedPoiIds = UserRepository.getVisitedPoiIds(uid)
        }
    }

    val handlePoiVisited: (PointOfInterest) -> Unit = { poi ->
        if (poi.id !in visitedPoiIds) {
            visitedPoiIds = visitedPoiIds + poi.id
            selectedPoi = poi

            val uid = AuthRepository.currentUser?.uid
            if (uid != null) {
                coroutineScope.launch {
                    try {
                        UserRepository.markPoiVisited(uid, poi.id)
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Failed to save visited POI: ${poi.id}", e)
                    }
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        // Displays the MapLibre map and fog overlay.
        MapLibreView(
            pois = pois,
            visitedPoiIds = visitedPoiIds,
            onPoiClick = { poi -> selectedPoi = poi },
            onPoiVisited = handlePoiVisited,
            modifier = Modifier.fillMaxSize()
        )
    }
    selectedPoi?.let { poi ->
        POIItem(
            poi = poi,
            isVisited = poi.id in visitedPoiIds,
            onDismissRequest = { selectedPoi = null }
        )
    }

}


/**
 * Displays an overlay bottom sheet on top of the active screen content.
 * Code example taken from: https://developer.android.com/reference/kotlin/androidx/compose/material3/ModalBottomSheet.composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIItem(
    poi: PointOfInterest,
    isVisited: Boolean,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.75f)
                .padding(20.dp)
        ) {
            Text(
                text = poi.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isVisited) "${poi.type} - Visited" else "${poi.type} - Unexplored",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = poi.description)
        }
    }
}